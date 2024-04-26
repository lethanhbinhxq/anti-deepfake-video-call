import requests

# Specify the URL of the server's endpoint
url = 'https://fefc886d-d832-4914-a230-a85be20cb9b9-00-3rmr3sqnunpfa.riker.replit.dev/uploadImage'

# Specify the path to the image file you want to upload
image_path = 'UserImage//ai-faces-01.jpg'

# Open the image file in binary mode
with open(image_path, 'rb') as file:
    # Prepare the data to be sent in the request
    files = {'file': file}

    # Send the POST request to the server
    response = requests.post(url, files=files)

# Check the response from the server
if response.status_code == 200:
    print('Image uploaded successfully')
else:
    print('Failed to upload image:', response.text)
