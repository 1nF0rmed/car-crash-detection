import requests,json
import firebase_admin
from firebase_admin import credentials
from firebase_admin import firestore
from firebase_admin import messaging
import uuid
import base64

def getLocation():
    payload = {'considerIp':'true'}
    jsonPayload = json.dumps(payload)
    #print jsonPayload
    headers = {'content-type': 'application/json'}
    privateKey = "<Your Google API private key>"
    url = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + privateKey
    r = requests.post(url,data=jsonPayload,headers = headers)
    response = json.loads(r.text)

    return (response['location']['lat'],response['location']['lng'],response['accuracy'])

def getBoxInfo():
    with open("info.dat", "r") as f:
        data = f.readline()
    return base64.b64decode(data.split(":")[1])

class DBMHandler:
    """
        Module to handle updating documents to database and 
        then updating messages using FCM to responder
    """

    def __init__(self):
        # Initialize firebase client
        cred = credentials.Certificate("car_crash.json")
        firebase_admin.initialize_app(cred)

        self.db = firestore.client()

    def send_to_responder(self, id):

        # Main group to send message
        topic = 'accident'

        # Get the coordinates
        crd = getLocation()

        message = messaging.Message(
            data = {
                "location":str(crd[0])+","+str(crd[1]),
                "acc_id": id
            },
            topic=topic
        )

        response = messaging.send(message)

        print("FCM Sent to responder", response)

    def update_datbase(self, data, id):
        # Get the coordinates
        crd = getLocation()

        print(data)
        # Create a document (record/tuple in MySQL) then upload to
        # firebase
        doc = {
            u'acc_id':unicode(id, "utf-8"),
            u'location':unicode(str(crd[0])+","+str(crd[1]), "utf-8"),
            u'sensor_data':unicode(data[0], "utf-8"),
            u'user_id':unicode(data[1], "utf-8")
        }
        print(doc)
        
        self.db.collection(u'accident').document().set(doc)
        

    def alert_and_push(self, data):
        # Accident id
        id = str(uuid.uuid4())
        self.send_to_responder(id)
        self.update_datbase(data, id)
