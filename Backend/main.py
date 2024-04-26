from flask import Flask, render_template, request, send_from_directory, session, redirect, url_for, jsonify
# from flask_socketio import join_room, leave_room, send, SocketIO
from flask_cors import CORS
import random
import werkzeug
from string import ascii_uppercase
from relative_recognition import *
from deepFakeRecognition import *
from sendEmail import *


app = Flask(__name__)
# CORS(app, origins='*')


@app.route("/", methods=["POST", "GET"])
def process_data():
    data = {
        'message': 'Hello from Flask server!',
        'numbers': [1, 2, 3, 4, 5]
    }
    # abcd = request.get_json()

    # Return the data as JSON response
    return jsonify(data)

@app.route("/mailVerify", methods=["POST", "GET"])
def mailVerify():
    # Return the data as JSON response
    sendMail()
    return 200

@app.route('/uploadImage', methods=['GET', 'POST'])
def uploadImage():
#   print(str(request.files))
    confidence, result = faceRecognition('UserImage/binh.jpg')
    if (confidence > 0.85):
        data = {
            'relative': 1,
            'name': result[2:]
        }
        
    else:
        data = {
            'relative': 0
        }
    file.save('UserImage/' + str(file.filename))
    return jsonify(data)
    # if 'file' not in request.files:
    #     return 'No file part in the request', 400
  
    # file = request.files['file']
    # if file.filename == '':
    #     return 'No selected file', 400
    
    # Process the image
    # For example, save it to a directory
    

@app.route('/checkDeepFake', methods=['GET', 'POST'])
def checkDeepFake():
    return jsonify(detect_deep_fake())


if __name__ == "__main__":
    app.run(host='0.0.0.0', port='8080')