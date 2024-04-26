import smtplib
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart

# Set up the email details
sender_email = "binh.lethanh@hcmut.edu.vn"
receiver_email = "thanhtinh1234789@gmail.com"
password = "nqtc om fjtz uif"
subject = "Verify mail from callee"
body = "This is a verification from Thanh Binh. If you receive this email and you are not in a call, please contact to warn your relatives who may be facing deepfake. Thank you./."

message = MIMEMultipart()
message["From"] = sender_email
message["To"] = receiver_email
message["Subject"] = subject
message.attach(MIMEText(body, "plain"))


def sendMail():
    with smtplib.SMTP_SSL("smtp.gmail.com", 465) as server:
        server.login(sender_email, password)
        server.sendmail(sender_email, receiver_email, message.as_string())

    print("Email sent successfully!")