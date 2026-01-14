import httpx
import openai
from livekit.agents import (
    DEFAULT_API_CONNECT_OPTIONS,
    APIConnectOptions,
    APIConnectionError,
    APIStatusError,
    APITimeoutError,
    tts,
    utils,
)


class SiliconflowTTS(tts.TTS):
    def __init__(
        self,
        *,
        model: str,
        voice: str,
        base_url: str,
        api_key: str,
        speed: float = 1.0,
        sample_rate: int = 44100,
        num_channels: int = 1,
        client: openai.AsyncClient | None = None,
    ) -> None:
        super().__init__(
            capabilities=tts.TTSCapabilities(streaming=False),
            sample_rate=sample_rate,
            num_channels=num_channels,
        )

        self._model = model
        self._voice = voice
        self._speed = speed
        self._client = client or openai.AsyncClient(
            max_retries=0,
            api_key=api_key,
            base_url=base_url,
            http_client=httpx.AsyncClient(
                timeout=httpx.Timeout(connect=15.0, read=30.0, write=30.0, pool=30.0),
                follow_redirects=True,
                limits=httpx.Limits(
                    max_connections=50,
                    max_keepalive_connections=50,
                    keepalive_expiry=120,
                ),
            ),
        )

    def synthesize(
        self,
        text: str,
        *,
        conn_options: APIConnectOptions = DEFAULT_API_CONNECT_OPTIONS,
    ) -> "tts.ChunkedStream":
        return _ChunkedStream(
            tts=self,
            input_text=text,
            conn_options=conn_options,
            client=self._client,
            model=self._model,
            voice=self._voice,
            speed=self._speed,
        )


class _ChunkedStream(tts.ChunkedStream):
    def __init__(
        self,
        *,
        tts: SiliconflowTTS,
        input_text: str,
        conn_options: APIConnectOptions,
        client: openai.AsyncClient,
        model: str,
        voice: str,
        speed: float,
    ) -> None:
        super().__init__(tts=tts, input_text=input_text, conn_options=conn_options)
        self._client = client
        self._model = model
        self._voice = voice
        self._speed = speed

    async def _run(self) -> None:
        request_id = utils.shortuuid()
        audio_bstream = utils.audio.AudioByteStream(
            sample_rate=self._tts.sample_rate,
            num_channels=self._tts.num_channels,
        )

        try:
            resp = await self._client.audio.speech.create(
                input=self.input_text,
                model=self._model,
                voice=self._voice,
                response_format="pcm",
                speed=self._speed,
                timeout=httpx.Timeout(60, connect=self._conn_options.timeout),
            )
            audio_bytes = await resp.aread()

            for frame in audio_bstream.write(audio_bytes):
                self._event_ch.send_nowait(
                    tts.SynthesizedAudio(
                        frame=frame,
                        request_id=request_id,
                    )
                )

            for frame in audio_bstream.flush():
                self._event_ch.send_nowait(
                    tts.SynthesizedAudio(
                        frame=frame,
                        request_id=request_id,
                    )
                )

        except openai.APITimeoutError:
            raise APITimeoutError()
        except openai.APIStatusError as e:
            msg = e.message
            if isinstance(e.body, dict):
                code = e.body.get("code")
                if code == 20047:
                    msg = f"{msg} (invalid voice: {self._voice!r}, model: {self._model!r})"
                elif code == 20012:
                    msg = f"{msg} (model: {self._model!r}, voice: {self._voice!r})"
            raise APIStatusError(
                msg,
                status_code=e.status_code,
                request_id=e.request_id,
                body=e.body,
            )
        except Exception as e:
            raise APIConnectionError() from e
