from ultralytics import YOLO
import os
# return {'deep_faked': True}
# Load a pretrained YOLOv8n model
model = YOLO('converted_keras//best.pt')

# Define path to the image file
source = 'testcase\/fake_44.jpg'

# Run inference on the source

def detect_deep_fake():
    results = model.predict(source)  # list of Results objects
    file_path = 'result\/result.txt'
    os.remove(file_path)
    results[0].save_txt(file_path)
    try:
        with open(file_path, 'r') as f:
            arguments = f.readlines()
    except FileNotFoundError:
        print(f"File not found: {file_path}")
    for arg in arguments:
        prob, tag = arg.strip().split()
        prob = float(prob)
        if tag == "Fake" and prob >= 0.7:
            return {'deep_fake': True}
        return {'deep_fake': False}

print(detect_deep_fake())