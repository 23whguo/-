import requests

url = "https://api.siliconflow.cn/v1/uploads/audio/voice"
headers = {
    "Authorization": "Bearer sk-tysxceqvlzddeysjqmdrfwlkbwazwnmibzhysjlghknvytqs" # 从 https://cloud.siliconflow.cn/account/ak 获取
}


files = {
    "file": open("D:\\Tomcat\\189467__speedenza__poem-darkness-voice.wav", "rb") # 参考音频文件
}
data = {
    "model": "fnlp/MOSS-TTSD-v0.5", # 模型名称
    "customName": "wanghappy-10s_2", # 参考音频名称
    "text": "我是王高兴,一名毕业于牛津大学的心理咨询师。今天来跟大家说一说我们这个圈子,我身边的朋友,大家的心理状态吧。" # 参考音频的文字内容
}

response = requests.post(url, headers=headers, files=files, data=data)

print(response.status_code)
print(response.json())  # 打印响应内容（如果是JSON格式）