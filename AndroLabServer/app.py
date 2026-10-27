import getopt
import json
import sys
from decimal import Decimal, InvalidOperation

from cheroot import wsgi
from flask import Flask, request

from database import db_session
from models import Account, User

app = Flask(__name__)

DEFAULT_PORT_NO = 8888


def usageguide():
    print("InsecureBankv2 Backend-Server")
    print("Options: ")
    print("  --port p    serve on port p (default 8888)")
    print("  --help      print this message")


@app.errorhandler(500)
def internal_servererror(error):
    print(" [!]", error)
    return "Internal Server Error", 500


@app.route('/login', methods=['POST'])
def login():
    response_msg = "fail"
    user = request.form['username']
    user_record = User.query.filter(
        User.username == request.form["username"]
    ).first()
    print("u=", user_record)
    if user_record and user_record.password == request.form["password"]:
        response_msg = "Correct Credentials"
    elif user_record and user_record.password != request.form["password"]:
        response_msg = "Wrong Password"
    elif not user_record:
        response_msg = "User Does not Exist"
    else:
        response_msg = "Some Error"
    data = {"message": response_msg, "user": user}
    print(json.dumps(data))
    return json.dumps(data)


@app.route('/getaccounts', methods=['POST'])
def getaccounts():
    response_msg = "fail"
    from_acc = to_acc = 0
    user = request.form['username']
    user_record = User.query.filter(User.username == user).first()
    if not user_record or user_record.password != request.form["password"]:
        response_msg = "Wrong Credentials so trx fail"
    else:
        response_msg = "Correct Credentials so get accounts will continue"
        accounts = Account.query.filter(Account.user == user)
        for account in accounts:
            if account.type == 'from':
                from_acc = account.account_number
            elif account.type == 'to':
                to_acc = account.account_number
    data = {"message": response_msg, "from": from_acc, "to": to_acc}
    print(json.dumps(data))
    return json.dumps(data)


@app.route('/changepassword', methods=['POST'])
def changepassword():
    response_msg = "fail"
    new_password = request.form['newpassword']
    user = request.form['username']
    print(new_password)
    user_record = User.query.filter(User.username == user).first()
    if not user_record:
        response_msg = "Error"
    else:
        response_msg = "Change Password Successful"
        user_record.password = new_password
        db_session.commit()
    data = {"message": response_msg}
    print(json.dumps(data))
    return json.dumps(data)


@app.route('/dotransfer', methods=['POST'])
def dotransfer():
    response_msg = "fail"
    user = request.form['username']
    amount = request.form['amount']
    from_acc = to_acc = 0
    user_record = User.query.filter(User.username == user).first()
    if not user_record or user_record.password != request.form["password"]:
        response_msg = "Wrong Credentials so trx fail"
    else:
        from_acc = request.form["from_acc"]
        to_acc = request.form["to_acc"]
        try:
            transfer_amount = Decimal(amount)
            if (
                not transfer_amount.is_finite()
                or transfer_amount <= 0
                or transfer_amount.quantize(Decimal("0.01")) != transfer_amount
            ):
                raise InvalidOperation
        except InvalidOperation:
            data = {
                "message": "Invalid amount",
                "from": from_acc,
                "to": to_acc,
                "amount": amount,
            }
            return json.dumps(data), 400
        from_account = Account.query.filter(
            Account.account_number == from_acc
        ).first()
        to_account = Account.query.filter(
            Account.account_number == to_acc
        ).first()
        to_account.balance += transfer_amount
        from_account.balance -= transfer_amount
        db_session.commit()
        response_msg = "Success"
    data = {
        "message": response_msg,
        "from": from_acc,
        "to": to_acc,
        "amount": amount,
    }
    return json.dumps(data)


@app.route('/devlogin', methods=['POST'])
def devlogin():
    user = request.form['username']
    data = {"message": "Correct Credentials", "user": user}
    print(json.dumps(data))
    return json.dumps(data)


if __name__ == '__main__':
    port = DEFAULT_PORT_NO
    options, args = getopt.getopt(sys.argv[1:], "", ["help", "port="])
    for option, argument in options:
        if option == "--help":
            usageguide()
            sys.exit(2)
        elif option == "--port":
            port = int(argument)

    server = wsgi.Server(("0.0.0.0", port), app, server_name='localhost')
    print("The server is hosted on port:", port)

    try:
        server.start()
    except KeyboardInterrupt:
        server.stop()
