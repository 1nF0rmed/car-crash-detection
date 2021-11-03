from flask import *
import firebase_admin
from collections import OrderedDict
from firebase_admin import credentials
from firebase_admin import firestore
import datetime
import pytz
import uuid

# Use a service account
cred = credentials.Certificate('car_crash.json')
firebase_admin.initialize_app(cred)
db = firestore.client()
app = Flask(__name__)

@app.route('/')
def home():
    return redirect('/login')

@app.route('/logout')
def logout():
    # Set forward to login
    resp = make_response(redirect('/login'))
    # remove cookie
    resp.set_cookie('log_session', expires=0)

    return resp

@app.route('/MngResponse', methods=['GET', 'POST'])
def mngresp():
    status = ""
    _sub = ""
    if request.method=="POST":
        # Get the subject name and total classes
        user = request.form["username"]
        password = request.form["password"]
        name = request.form["name"]
        location = request.form["location"]
        coordinates = request.form["coor"]

        data = {
            u'username': user,
            u'password': password,
            u'name': name,
            u'location': location,
            u'coor': coordinates
        }

        # Add data to database
        db.collection(u'responder_info').document(user).set(data)

        print("[LOG] Updated to database")

        status = "<h2 style='color: green;font-weight: bolder;'>Updated to database</h2>"

    _sub = "<table class='table' border='2px'><tr><th>Responder ID</th><th>Location</th></tr>"
    # Get all the subjects
    subs = db.collection(u'responder_info').stream()
    for sub in subs:
        sub = sub.to_dict()
        _sub = _sub + "<tr><td>"+sub["username"]+"</td><td>"+sub["location"]+"</td></tr>"
    
    _sub = _sub + "</table>"

    return render_template("reg_resp.html", status=status, sub=_sub)

@app.route('/MngAccident', methods=['GET'])
def viewtable():
    
    docs = db.collection(u'accident').stream()
    table = "<table class='table' border='2px' style='border-spacing: 10px;'><tr><th>Accident ID</th><th>Location ID</th><th>User ID</th></tr>"
    for doc in docs:
        doc = doc.to_dict()
        table = table+"<tr><td>"+doc["acc_id"]+"</td>"
        table = table + "<td>"+doc["location"]+"</td>"
        table = table + "<td>"+doc["user_id"] + "</td>"
        table = table+"</tr>"
    
    table = table + "</table>"

    return render_template("accident.html", table=table) 

@app.route('/sendSMS', methods=['GET', 'POST'])
def sendSMS():
    return "SENT SMS"

@app.route('/login', methods=['GET', 'POST'])
def login():
    error = None
    if request.method == "POST":
        error = 'Invalid Credentials. Please try again.'
        # Check if user and password is admin
        if request.form['username']=='admin' and request.form['password'] =='admin':
            # If correct, then forward to dashboard
            resp = redirect(url_for('dash'))
            # Set the session cookie
            resp.set_cookie('log_session', 'admin:'+str(uuid.uuid4()))
            return resp
        else:
            # Check credentials in database
            user_doc = db.collection(u'responder_info').where(u'username', u'==', request.form['username']).limit(1).stream()
            if user_doc is not None:
                password = ""
                for doc in user_doc:
                    doc = doc.to_dict()
                    # Get the stored password
                    resp = request.form['password']
                    print("[LOG] Password: "+resp+"|")
                    _password = doc["password"]
                    print("[LOG] Got password: "+_password+"|")
                    if resp == _password:
                        # If correct, then forward to dashboard
                        resp = redirect(url_for('resdash'))
                        # Set the session cookie
                        resp.set_cookie('log_session', request.form['username']+':'+str(uuid.uuid4()))

                        return resp


    return render_template('login.html', error=error)

@app.route('/register', methods=['GET', 'POST'])
def register():
    return render_template('register.html')

@app.route('/dashboard', methods=['GET', 'POST'])
def dash():
    # Get the session cookie
    cookie = request.cookies.get("log_session")

    # Check if admin not in the cookie
    if cookie is None or "admin" not in cookie:
        return redirect(url_for('login'))
    else:
        user = cookie.split(":")[0]
        # If cookie has admin then display the dashboard page
        return render_template('dashboard.html', user=user)

@app.route('/resdash', methods=['GET', 'POST'])
def resdash():
    # Get the session cookie
    cookie = request.cookies.get("log_session")

    # Check if admin not in the cookie
    if cookie is None or "admin" in cookie:
        return redirect(url_for('login'))
    else:
        user = cookie.split(":")[0]
        # If cookie has admin then display the dashboard page
        return render_template('reg_dash.html', user=user)

if __name__ == '__main__':
    app.run(debug=True)