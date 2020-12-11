import sqlite3
import json
import base64
import tornado.ioloop
import tornado.web

def b64ToNormal(inputStr : str)->str :
    if inputStr=="":
        return ""
    return base64.b64decode(inputStr.encode("ascii")).decode("ascii")

def normalTob64(inputStr :str)->str :
    if inputStr=="":
        return ""
    return base64.b64encode(inputStr.encode("ascii")).decode("ascii")

class DbTable :

    def __init__(self):
        self.conn = sqlite3.connect("main.db")
        self.c=self.conn.cursor()
        try:
            self.c.execute("SELECT * FROM bssidloc")
            self.c.execute("SELECT * FROM studentbssid")
        except sqlite3.OperationalError :
            self.c.execute("CREATE TABLE studentbssid(id TEXT NOT NULL PRIMARY KEY,password TEXT NOT NULL,bssid TEXT,whitelist TEXT)")
            self.c.execute("CREATE TABLE bssidloc(bssid TEXT NOT NULL PRIMARY KEY,location TEXT)")
            self.conn.commit()

    def checkCreds(self,studentID:str,password:str)->dict:
        b64_password = normalTob64(password)
        out = list(self.c.execute(f"SELECT password FROM studentbssid WHERE id LIKE '{studentID}'"))
        #print(out)
        if len(out)==0 :
            return {
                    "msg":"NO_SUCH_USER"
                    }
        else :
            if out[0][0] == normalTob64(password):
                return {
                        "msg":"AUTH_SUCCESS"
                        }
            else:
                return {
                        "msg":"AUTH_FAIL"
                        }

    def getWhiteList(self,studentID:str):
        command = f"SELECT whitelist FROM studentbssid WHERE id LIKE '{studentID}'"
        #print(command)
        out = list(self.c.execute(command))
        #print(out)
        if len(out)==0 :
            return {
                    "msg":"GET_WHITELIST_FAIL"
                    }
        else:
            dat = b64ToNormal(out[0][0])
            return{
                "msg":"GET_WHITELIST_SUCCESS",
                "data":dat
            }

    def getBSSIDLocation(self,bssid:str):
        b64_bssid = normalTob64(bssid)
        out = list(self.c.execute(f"SELECT location FROM bssidloc WHERE bssid LIKE '{b64_bssid}'"))
        if len(out)==0 :
            return {
                    "msg":"GET_LOCATION_FAILED"
                    }
        else:
            return {
                    "msg":"GET_LOCATION_SUCCESS",
                    "data":b64ToNormal(out[0][0])
                    }
    def checkBSSIDPresence(self,bssid:str):
        if self.getBSSIDLocation(bssid)["msg"]=="GET_LOCATION_SUCCESS" :
            return {
                    "msg":"BSSID_PRESENCE_CONFIRMED"
                    }
        else:
            return {
                    "msg":"BSSID_ABSENCE_CONFIRMED"
                    }
    def checkStudentIDPresence(self,studentID):
        result = self.getWhiteList(studentID)
        if result["msg"]=="GET_WHITELIST_SUCCESS":
            return{
                "msg":"STUDENT_PRESENCE_CONFIRMED"
            }
        return{
            "msg":"STUDENT_ABSENCE_CONFIRMED"
        }

    def getStudentBSSID(self,studentID):
        if self.checkStudentIDPresence(studentID)['msg']=="STUDENT_PRESENCE_CONFIRMED":
            #print(studentID)
            out = list(self.c.execute(f"SELECT bssid FROM studentbssid WHERE id LIKE '{studentID}'"))
            #print(out)
            if len(out)==0 :
                return{
                        "msg":"BSSID_NOT_AVAILABLE"
                        }
            else:
                #print(b64ToNormal(out[0][0]))
                return{
                    "msg":"GET_BSSID_SUCCESS",
                    "data":b64ToNormal(out[0][0])
                    }
        else:
            return{
                    "msg":"GET_BSSID_FAIL"
                    }

    def getFriendLocation(self,requestStudentID,targetStudentID): # GET FRIEDN LOCATION MODUEL
        result = self.getWhiteList(targetStudentID)
        if result['msg'] == "GET_WHITELIST_SUCCESS" :
            whitelist = result["data"]
            if requestStudentID in whitelist :
                result = self.getStudentBSSID(targetStudentID)
                #print("p",targetStudentID,result)
                if result["msg"]=="GET_BSSID_SUCCESS" and result["data"]!="":
                    bssid = result["data"]
                    result = self.getBSSIDLocation(bssid)
                    if result["msg"]=="GET_LOCATION_SUCCESS":
                        return{
                            "msg":"GET_LOCATION_SUCCESS",
                            "data":result["data"]
                            }

            else:
                return{
                        "msg":"UNAUTHORIZED_ERROR"
                        }
        return{
                "msg":"GET_LOCATION_FAILED"
        }

    def updateStudentBSSID(self,studentID,bssid): #UPDATE LOCATION MDOULE
        result1 = self.checkStudentIDPresence(studentID)["msg"]
        result2 = self.checkBSSIDPresence(bssid)["msg"]
        #print(result2,bssid,len(bssid))
        if result1=="STUDENT_PRESENCE_CONFIRMED" and result2=="BSSID_PRESENCE_CONFIRMED":
            b64_bssid = normalTob64(bssid)
            self.c.execute(f"UPDATE studentbssid SET bssid='{b64_bssid}' WHERE id LIKE '{studentID}'")
            self.conn.commit()
            return{
                "msg":"BSSID_UPDATION_SUCCESS"
                }
        else:
            return{
                    "msg":"BSSID_UPDATION_FAIL" #addBSSIDLocation must be called
                }

    def addBSSIDLocation(self,bssid,location):
        result = self.checkBSSIDPresence(bssid)["msg"]
        #print(result,bssid,len(bssid))
        if result=="BSSID_ABSENCE_CONFIRMED" :
            self.c.execute("INSERT INTO bssidloc VALUES(?,?)",(normalTob64(bssid),normalTob64(location)))
            self.conn.commit()
            return {
                    "msg":"ADD_BSSID_LOCATION_SUCCESS"
                    }
        else:
            return {
                    "msg":"ADD_BSSID_LOCATION_FAIL"
                    }

    def createUser(self,studentID:str,password:str):
        if self.checkStudentIDPresence(studentID)["msg"]=="STUDENT_ABSENCE_CONFIRMED" :
            self.c.execute("INSERT INTO studentbssid VALUES(?,?,?,?)",(studentID,normalTob64(password),"",""))
            self.conn.commit()
            return {
                    "msg":"ADD_USER_SUCCESS"
                    }
        else :
            return {
                    "msg":"ADD_USER_FAILED"
                    }
    
    def updateWhitelist(self,studentID:str,whitelist:str):
        if self.checkStudentIDPresence(studentID)["msg"]=="STUDENT_PRESENCE_CONFIRMED" :
            #print(whitelist,studentID)
            b64_whitelist = normalTob64(whitelist)
            command = f"UPDATE studentbssid SET whitelist='{b64_whitelist}' WHERE id LIKE '{studentID}'"
            #print(command)
            self.c.execute(command)
            self.conn.commit()
            #print(list(self.c.execute(f"SELECT whitelist FROM studentbssid WHERE id LIKE '{studentID}'")))
            return{
                    "msg":"UPDATE_WHITELIST_SUCCESS"
                    }
        else:
            return{
                    "msg":"UPDATE_WHITELIST_FAILED"
                    }

table = DbTable()

#table.getTableData()

#print(list(table.conn.execute("pragma table_info('bssidloc')")))
#print(list(table.conn.execute("pragma table_info('studentbssid')")))

#print(table.createUser("abc","123456789"))
#print(table.checkCreds("abc","123456789"))
#print(table.checkCreds("abc",'1234567'))
#print(table.checkCreds('abcd','123456789'))
table.getWhiteList('abcd')

class MainHandler(tornado.web.RequestHandler):
    def get(self):
        self.write("-1")

    def post(self):
        print("Incoming Request")
        self.set_header("Content-Type","application/json")
        body=self.request.body.decode("ascii")
        print(body)
        if(body==""):
            self.write("-1")
            return
        request = json.loads(self.request.body.decode())

        if request["mode"]!="CREATE_USER":
            if table.checkCreds(request["info"]["studentID"],request["info"]["password"])["msg"]=="AUTH_SUCCESS":

                if request["mode"]=="AUTH_USER" :
                    self.write(json.dumps({"msg":"SUCCESS"}))
                    return

                elif request["mode"]=="UPDATE_LOCATION" :
                    result = table.updateStudentBSSID(request["info"]["studentID"],request["info"]["bssid"])
                    if result['msg']=="BSSID_UPDATION_SUCCESS" :
                        self.write({"msg":"UPDATION_SUCCESS"})
                    else:
                        self.write(json.dumps({
                            "msg":"BSSID_DOESNT_EXISTS"
                            }))
                elif request["mode"]=="GET_FRIEND_LOCATION":
                    result = table.getFriendLocation(request["info"]["studentID"],request["info"]["otherStudentID"])
                    if result["msg"]=="GET_LOCATION_SUCCESS":
                        self.write(json.dumps(
                                {
                                "msg":"SUCCESS",
                                "data":result["data"]
                                }))
                    else:
                        self.write(json.dumps({
                            "msg":"NOT_IN_WHITELIST_ERROR"
                        }))
                elif request["mode"]=="ADD_BSSID_LOCATION":
                    result = table.addBSSIDLocation(request["info"]["bssid"],request["info"]["location"])
                    if result['msg']=="ADD_BSSID_LOCATION_SUCCESS" :
                        self.write(json.dumps(
                            {
                                "msg":"SUCCESS"
                                }
                            ))
                    else:
                        self.write(json.dumps(
                            {
                                "msg":"FAILED"
                                }
                            ))
                elif request["mode"]=="GET_WHITELIST" :
                    #print("id",request["info"]["studentID"])
                    result = table.getWhiteList(request["info"]["studentID"])
                    if result['msg']=="GET_WHITELIST_SUCCESS" :
                        self.write(json.dumps({
                            "msg":"SUCCESS",
                            "data":result["data"]
                            }))
                    else:
                        self.write(json.dumps({
                            "msg":"FAILED"
                            }))
                elif request["mode"]=="UPDATE_WHITELIST":
                    result = table.updateWhitelist(request["info"]["studentID"],request["info"]["whitelist"])
                    if result["msg"]=="UPDATE_WHITELIST_SUCCESS" :
                        self.write(json.dumps({
                            "msg":"SUCCESS"
                            }))
                    else:
                        self.write(json.dumps({
                            "msg":"FAILED"
                            }))
            else:
                self.write(json.dumps({
                    "msg":"AUTH_FAIL"
                    }))
        else:
            if table.createUser(request["info"]["studentID"],request["info"]["password"])["msg"]=="ADD_USER_SUCCESS" :
                self.write(json.dumps({
                    "msg":"ADD_USER_SUCCESS"
                    }))
            else:
                self.write(
                        json.dumps({
                            "msg":"ADD_USER_FAILED"
                            }
                        ))

def make_app():
    return tornado.web.Application([
        (r"/", MainHandler),
    ])

if __name__ == "__main__":
    app = make_app()
    app.listen(8888)
    tornado.ioloop.IOLoop.current().start()

