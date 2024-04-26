import requests

API_URL = "https://api-inference.huggingface.co/models/prithivMLmods/Deep-Fake-Detector-Model"
API_TOKEN = "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"
headers = {"Authorization": f"Bearer {API_TOKEN}"}

def query(filename):
    with open(filename, "rb") as f:
        data = f.read()
    response = requests.post(API_URL, headers=headers, data=data)
    return response.json()

output = query("UserImage//ai-faces-01.jpg")
print(output)