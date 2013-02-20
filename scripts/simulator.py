import json, time
import urllib, urllib2
import random
import threading
from datetime import datetime, timedelta

version="1.1"

startDate=datetime(2012,12,1,0,0,0,0)
endDate=datetime(2012,12,31,23,59,59,999)

user="as_test1"
password="password1"
client="as_script"
stream_version=14
observer_version=14
ts_header="test1"
observer_id="org.ohmage.probes.audioSensProbe"
global auth_token
global dataArr

class FuncThread(threading.Thread):
    def __init__(self, target, *args):
        self._target = target
        self._args = args
        threading.Thread.__init__(self)
 
    def run(self):
        self._target(*self._args)
        
#auth
def auth():
    params = urllib.urlencode({'user': user, 'password': password, 'client': client})
    url = 'https://test.ohmage.org/app/user/auth_token'
    req = urllib2.Request(url, params)
    global auth_token
    while True:
        response = urllib2.urlopen(req)
        auth_obj = json.loads(response.read())
        if auth_obj['result'] == "success":
            auth_token = auth_obj['token']
            print("Authenticated")
            return
        print("Auth failed, retrying...")
        time.sleep(3)
        
def upload(obj):
    global auth_token
    dataObj= json.dumps(obj)
    print dataObj
    params = urllib.urlencode({ 'client': client,
                               'auth_token': auth_token, 
                               'observer_id':observer_id, 
                               'observer_version':observer_version,
                               'data':dataObj})
    url = 'https://test.ohmage.org/app/stream/upload'
    req = urllib2.Request(url, params)
    while True:
        response = urllib2.urlopen(req)
        resp_obj = json.loads(response.read())
        if resp_obj['result'] == "success":
            print "uploaded"+str(json.dumps(resp_obj))
            return
        print("retrying uploading....:"+str(resp_obj))
        auth_token = auth()
        time.sleep(3)
        
def generateArr(duration, dataType):
    if dataType == "int":
        return [int(1000*random.random()) for i in xrange(duration * 64)]
    elif dataType == "float":
        return [1000*random.random() for i in xrange(duration * 64)]
    else:
        return []
         

def getFeatureObject(timestamp, id_index, duration, featureName, subfeatureNames, dataType="int"):
    obj = dict()
    obj['stream_id'] = "features"
    obj['stream_version'] = stream_version
    metadata=dict()
    metadata['id'] = str(int(time.mktime(timestamp.timetuple())*1000))+"_"+ts_header+"_"+str(id_index)
    metadata['timestamp'] = timestamp.isoformat()+"-08:00"
    
    feature=dict()
    feature['version']=version
    feature['frameNo']=int(time.mktime(timestamp.timetuple())*1000)
    feature['name']=featureName
    feature['featureArray'] = []
    for subfeatureName in subfeatureNames:
        subfeature=dict()
        subfeature['name']=subfeatureName
        subfeature['data']=generateArr(duration, dataType)
        subfeature['summary']=dict()
        subfeature['summary']['average']=2
        feature['featureArray'].append(subfeature)
    
    obj['metadata']=metadata
    obj['data']=feature
    return obj

def getSummaryObject(timestamp):
    end = timestamp + timedelta(hours=1)
    
    obj = dict()
    obj['stream_id'] = "summarizers"
    obj['stream_version'] = stream_version
    metadata=dict()
    metadata['id'] = str(int(time.mktime(timestamp.timetuple())*1000))+"sum"+ts_header+""+str(random.randint(0,999))
    metadata['timestamp'] = timestamp.isoformat()+"-08:00"
    
    outer=dict()
    outer['version']=version
    outer['frameNo']=int(time.mktime(timestamp.timetuple())*1000)
    outer['summarizer']="hourly"
    outer['end'] = int(time.mktime(end.timetuple())*1000)
    
    count_total = 0
    count_missing = 0
    count_speech = 0
    count_silence = 0

    countArr=[0] * 60
    inferenceArr=[0] * 60
    for i in range(0,60):
        countArr[i] = random.randint(0,2)
        count_total = count_total + countArr[i]
        if countArr[i] == 0:
            inferenceArr[i] =-1
            count_missing = count_missing + 1
        else:
            inferenceArr[i] =  random.randint(0,1)
            if inferenceArr[i] == 0:
                count_silence = count_silence + 1
            else:
                count_speech = count_speech + 1

    dataOb=dict()
    dataOb["countArr"] = countArr
    dataOb["inferenceArr"] = inferenceArr
    summary=dict()
    summary["count_total"] = count_total
    summary["count_silent"] = count_silence
    summary["count_speech"] = count_speech           
    summary["count_missing"] = count_missing          
    
    outer["summary"] = summary
    outer["data"] = dataOb  
    
    obj["metadata"]=metadata
    obj['data']=outer
    return obj

 
auth()       
print(auth_token)
dataArr = []        
current = startDate

while current < endDate:
    duration=10
    #Features,sensors,classifiers

    '''
    dataArr.append(getFeatureObject(current,1, duration,"Energy",["Energy"]))
    dataArr.append(getFeatureObject(current,2, duration,"ZeroCrossingRate",["ZeroCrossingRate"]))
    dataArr.append(getFeatureObject(current,3, duration,
                                    "SpeechInferenceFeatures",
                                    ["NoOfCorrelationPeaks",
                                     "MaxCorrelationPeakValue",
                                     "MaxCorrelationPeakLag",
                                     "SpectralEntropy",
                                     "RelativeSpectralEntropy"],dataType='float'))
    dataArr.append(getFeatureObject(current,4, duration,
                                "MFCC",
                                ["MFCC0",
                                 "MFCC1",
                                 "MFCC2",
                                 "MFCC3",
                                 "MFCC4",
                                 "MFCC5",
                                 "MFCC6",
                                 "MFCC7",
                                 "MFCC8",
                                 "MFCC9",
                                 "MFCC10",
                                 "MFCC11"],dataType='float'))
    '''
    
    if current.minute == 0:
        print current
        dataArr.append(getSummaryObject(current))
    
    #print dataArr
    #break
    
    if len(dataArr)>10:
        print("Uploading"+str(threading.active_count()))
        #threading.Thread(target=upload, args=(list(dataArr),)).start()
        while threading.active_count()>5:
            print("Too many threads, waiting...")
            time.sleep(3)
        FuncThread(upload, list(dataArr)).start()
        #upload(dataArr)
        dataArr = []
    #print(dataArr)
    #event
    #summarizer
    current = current + timedelta(seconds=60)
