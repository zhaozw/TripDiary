#include <jni.h>
#include <string.h>
#include <stdio.h>
#include <fstream>
#include <iostream>
#include <vector>
#include <cstdlib>
#include <cmath>
#include <sstream>
#include <ctime>
#include <android/log.h>

#define pi 3.14159265358979323846
#define earthRadius 6378.1*1000
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO,"trip",__VA_ARGS__)

using namespace std;

extern "C" {

jclass cacheClass;
jclass myLatLng2Class;
jclass arrayListClass;
jclass floatClass;
jclass gpxAnalyzer2Class;

jfieldID mLatitude;
jfieldID mLongitude;
jfieldID mAltitude;
jfieldID mTime;

jfieldID mLats;
jfieldID mStartTime;
jfieldID mEndTime;
jfieldID mTotalTime;
jfieldID mDistance;   // meters
jfieldID mAvgSpeed;   // km/hr
jfieldID mMaxSpeed;		 // km/hr
jfieldID mClimb;      // meters
jfieldID mMaxAltitude;      // meters
jfieldID mMinAltitude;      // meters

jmethodID struct_MyLatLng2;
jmethodID struct_ArrayList;
jmethodID struct_Float;
jmethodID arrayList_add;
jmethodID progress_changed;
bool stop;

bool startsWith(const string& haystack, const string& needle) {
	return needle.length() <= haystack.length() && equal(needle.begin(), needle.end(), haystack.begin());
}
bool contains(const string& haystack, const string& needle) {
	return haystack.find(needle) != string::npos;
}
vector<string> split(string str, string sep) {
	char* cstr = const_cast<char*>(str.c_str());
	char* current;
	vector<string> arr;
	current = strtok(cstr, sep.c_str());
	while (current != NULL) {
		arr.push_back(current);
		current = strtok(NULL, sep.c_str());
	}
	return arr;
}
double distFrom(double lat1, double lng1, double lat2, double lng2) {
	double dLat, dLng, dist;
	dLat = (lat2 - lat1) * pi / 360;
	dLng = (lng2 - lng1) * pi / 360;
	dist = sin(dLat) * sin(dLat) + cos(pi * lat1 / 180) * cos(pi * lat2 / 180) * sin(dLng) * sin(dLng);
	dist = 2 * atan2(sqrt(dist), sqrt(1 - dist));
	dist = earthRadius * dist;
	return dist;
}
static double now_ms(void) {

    struct timespec res;
    clock_gettime(CLOCK_REALTIME, &res);
    return 1000.0 * res.tv_sec + (double) res.tv_nsec / 1e6;

}
class MyLatLng2 {
public:
	double lat;
	double lng;
	string time;
	float altitude;
};
jint JNI_OnLoad(JavaVM* vm, void* reserved) {
	JNIEnv* env;
	if (vm->GetEnv(reinterpret_cast<void**>(&env), JNI_VERSION_1_6) != JNI_OK) {
		return -1;
	}
	cacheClass = env->FindClass("com/yupog2003/tripdiary/data/TrackCache");
	cacheClass = (jclass) env->NewGlobalRef(cacheClass);
	myLatLng2Class = env->FindClass("com/yupog2003/tripdiary/data/MyLatLng2");
	myLatLng2Class = (jclass) env->NewGlobalRef(myLatLng2Class);
	arrayListClass = env->FindClass("java/util/ArrayList");
	arrayListClass = (jclass) env->NewGlobalRef(arrayListClass);
	floatClass = env->FindClass("java/lang/Float");
	floatClass = (jclass) env->NewGlobalRef(floatClass);
	gpxAnalyzer2Class = env->FindClass("com/yupog2003/tripdiary/data/GpxAnalyzer2");
	gpxAnalyzer2Class = (jclass) env->NewGlobalRef(gpxAnalyzer2Class);

	mLatitude = env->GetFieldID(myLatLng2Class, "latitude", "D");
	mLongitude = env->GetFieldID(myLatLng2Class, "longitude", "D");
	mAltitude = env->GetFieldID(myLatLng2Class, "altitude", "F");
	mTime = env->GetFieldID(myLatLng2Class, "time", "Ljava/lang/String;");

	mLats = env->GetFieldID(cacheClass, "lats", "Ljava/util/ArrayList;");
	mStartTime = env->GetFieldID(cacheClass, "startTime", "Ljava/lang/String;");
	mEndTime = env->GetFieldID(cacheClass, "endTime", "Ljava/lang/String;");
	mTotalTime = env->GetFieldID(cacheClass, "totalTime", "Ljava/lang/String;");
	mDistance = env->GetFieldID(cacheClass, "distance", "F");   // meters
	mAvgSpeed = env->GetFieldID(cacheClass, "avgSpeed", "F");   // km/hr
	mMaxSpeed = env->GetFieldID(cacheClass, "maxSpeed", "F");		 // km/hr
	mClimb = env->GetFieldID(cacheClass, "climb", "F");      // meters
	mMaxAltitude = env->GetFieldID(cacheClass, "maxAltitude", "F");      // meters
	mMinAltitude = env->GetFieldID(cacheClass, "minAltitude", "F");      // meters

	struct_MyLatLng2 = env->GetMethodID(myLatLng2Class, "<init>", "()V");
	struct_ArrayList = env->GetMethodID(arrayListClass, "<init>", "()V");
	struct_Float = env->GetMethodID(floatClass, "<init>", "(F)V");
	arrayList_add = env->GetMethodID(arrayListClass, "add", "(Ljava/lang/Object;)Z");
	progress_changed = env->GetMethodID(gpxAnalyzer2Class, "onProgressChanged", "(J)V");
	return JNI_VERSION_1_6;
}
JNIEXPORT jboolean Java_com_yupog2003_tripdiary_data_GpxAnalyzer2_parse(JNIEnv* env, jobject thiz, jstring gpxPath, jobject cache, jobject speeds, jint timezoneO) {
	stop = false;
	const char *gpxPathChar = (char*) (env->GetStringUTFChars(gpxPath, 0));
	char cachePath[100];
	strcpy(cachePath, gpxPathChar);
	strcat(cachePath, ".cache");
	time_t timezoneOffset = timezoneO; //(second)
	ifstream fis(gpxPathChar, ifstream::in);
	string s;
	vector<MyLatLng2> track;
	vector<time_t> times;
	MyLatLng2 *latlng;
	MyLatLng2 preLatLng;
	bool first = true;
	float distance = 0;
	float totalAltitude = 0;
	float maxAltitude = 0;
	float minAltitude = 1E+37;
	float preAltitude = 0;
	unsigned int count = 0;
	jobject latsObject = env->NewObject(arrayListClass, struct_ArrayList);
	while (getline(fis, s)) {
		if (stop)
			return false;
		count++;
		if (count == 5000) {
			env->CallVoidMethod(thiz, progress_changed, (jlong) fis.tellg());
			count = 0;
		}
		if (contains(s, "<trkpt")) {
			latlng = new MyLatLng2;
			char *c = const_cast<char*>(s.c_str());
			if (s.find("lat") > s.find("lon")) {
				strtok(c, "\"");
				latlng->lng = atof(strtok(NULL, "\""));
				strtok(NULL, "\"");
				latlng->lat = atof(strtok(NULL, "\""));
			} else {
				strtok(c, "\"");
				latlng->lat = atof(strtok(NULL, "\""));
				strtok(NULL, "\"");
				latlng->lng = atof(strtok(NULL, "\""));
			}
			free(c);
		} else if (contains(s, "<ele>")) {
			float altitude = atof(s.substr(s.find(">") + 1, s.rfind("<") - s.find(">") - 1).c_str());
			latlng->altitude = altitude;
			if (altitude > maxAltitude)
				maxAltitude = altitude;
			if (altitude < minAltitude)
				minAltitude = altitude;
		} else if (contains(s, "<time>")) {
			string timeStr = s.substr(s.find(">") + 1, s.rfind("Z") - s.find(">") - 1);
			tm t;
			strptime(timeStr.c_str(), "%Y-%m-%dT%H:%M:%S", &t);
			t.tm_isdst = 0;
			char resultTime[80];
			time_t seconds = mktime(&t) + timezoneOffset;
			strftime(resultTime, 80, "%Y-%m-%dT%H:%M:%S", localtime(&seconds));
			latlng->time = (string) resultTime;
			times.push_back(seconds);
		} else if (contains(s, "</trkpt>")) {
			if (!first) {
				float altitudeDiffer = latlng->altitude - preAltitude;
				if (abs(altitudeDiffer) > 15) {
					if (altitudeDiffer > 0)
						totalAltitude += altitudeDiffer;
					preAltitude = latlng->altitude;
				}
				distance += distFrom(preLatLng.lat, preLatLng.lng, latlng->lat, latlng->lng);
			} else {
				first = false;
				preLatLng = *latlng;
				preAltitude = latlng->altitude;
			}
			preLatLng = *latlng;
			track.push_back(*latlng);

			jobject myLatLng2 = env->NewObject(myLatLng2Class, struct_MyLatLng2);
			env->SetDoubleField(myLatLng2, mLatitude, latlng->lat);
			env->SetDoubleField(myLatLng2, mLongitude, latlng->lng);
			env->SetFloatField(myLatLng2, mAltitude, latlng->altitude);
			jstring time = env->NewStringUTF(latlng->time.c_str());
			env->SetObjectField(myLatLng2, mTime, time);
			env->CallBooleanMethod(latsObject, arrayList_add, myLatLng2);
			env->DeleteLocalRef(time);
			env->DeleteLocalRef(myLatLng2);
			free(latlng);
		}
	}
	fis.close();
	int trackSize = track.size();
	int timesSize = times.size();
	if (trackSize == 0 || timesSize == 0 || stop)
		return false;
	float maxSpeed = 0;
	for (int i = 0; i + 20 < trackSize && i + 20 < timesSize; i += 20) {
		if (stop)
			return false;
		float dist = distFrom(track[i].lat, track[i].lng, track[i + 20].lat, track[i + 20].lng);
		float seconds = times[i + 20] - times[i];
		float speed = dist / seconds * 18 / 5;
		if (speeds != NULL) {
			jobject speedFloat = env->NewObject(floatClass, struct_Float, (jfloat) speed);
			env->CallBooleanMethod(speeds, arrayList_add, speedFloat);
			env->DeleteLocalRef(speedFloat);
		}
		if (maxSpeed < speed)
			maxSpeed = speed;

	}
	if (stop)
		return false;
	time_t totalSeconds = times[times.size() - 1] - times[0];
	stringstream sss;
	int day = totalSeconds / 86400;
	int hour = totalSeconds % 86400 / 3600;
	int min = totalSeconds % 3600 / 60;
	int sec = totalSeconds % 60;
	if (day != 0) {
		sss << day << "T";
	}
	sss << hour << ":" << min << ":" << sec;
	string totalTime = sss.str();
	float avgSpeed = distance / totalSeconds * 18 / 5;

	env->SetObjectField(cache, mLats, latsObject);
	jstring jstartTime = env->NewStringUTF(track[0].time.c_str());
	jstring jendTime = env->NewStringUTF(track[track.size() - 1].time.c_str());
	jstring jtotalTime = env->NewStringUTF(totalTime.c_str());
	env->SetObjectField(cache, mStartTime, jstartTime);
	env->SetObjectField(cache, mEndTime, jendTime);
	env->SetObjectField(cache, mTotalTime, jtotalTime);
	env->SetFloatField(cache, mDistance, distance);
	env->SetFloatField(cache, mAvgSpeed, avgSpeed);
	env->SetFloatField(cache, mMaxSpeed, maxSpeed);
	env->SetFloatField(cache, mClimb, totalAltitude);
	env->SetFloatField(cache, mMaxAltitude, maxAltitude);
	env->SetFloatField(cache, mMinAltitude, minAltitude);
	env->DeleteLocalRef(latsObject);
	env->DeleteLocalRef(jstartTime);
	env->DeleteLocalRef(jendTime);
	env->DeleteLocalRef(jtotalTime);

	ofstream fos(cachePath, ofstream::out);
	fos.precision(12);
	fos << track[0].time << endl;
	fos << track[track.size() - 1].time << endl;
	fos << totalTime << endl;
	fos << distance << endl;
	fos << avgSpeed << endl;
	fos << maxSpeed << endl;
	fos << totalAltitude << endl;
	fos << maxAltitude << endl;
	fos << minAltitude << endl;
	for (int i = 0; i < trackSize; i++) {
		fos << track[i].lat << endl;
		fos << track[i].lng << endl;
		fos << track[i].altitude << endl;
		fos << track[i].time << endl;
	}
	fos.close();
	return true;
}
JNIEXPORT bool Java_com_yupog2003_tripdiary_data_GpxAnalyzer2_getCache(JNIEnv* env, jobject thiz, jstring cachePath, jobject cache) {
	const char *cachePathChar = (char*) (env->GetStringUTFChars(cachePath, 0));
	ifstream fis(cachePathChar);
	string s;
	getline(fis, s);
	jstring jstartTime = env->NewStringUTF(s.c_str());
	getline(fis, s);
	jstring jendTime = env->NewStringUTF(s.c_str());
	getline(fis, s);
	jstring jtotalTime = env->NewStringUTF(s.c_str());
	env->SetObjectField(cache, mStartTime, jstartTime);
	env->SetObjectField(cache, mEndTime, jendTime);
	env->SetObjectField(cache, mTotalTime, jtotalTime);
	getline(fis, s);
	env->SetFloatField(cache, mDistance, (jfloat)atof(s.c_str()));
	getline(fis, s);
	env->SetFloatField(cache, mAvgSpeed, (jfloat)atof(s.c_str()));
	getline(fis, s);
	env->SetFloatField(cache, mMaxSpeed, (jfloat)atof(s.c_str()));
	getline(fis, s);
	env->SetFloatField(cache, mClimb, (jfloat)atof(s.c_str()));
	getline(fis, s);
	env->SetFloatField(cache, mMaxAltitude, (jfloat)atof(s.c_str()));
	getline(fis, s);
	env->SetFloatField(cache, mMinAltitude, (jfloat)atof(s.c_str()));
	env->DeleteLocalRef(jstartTime);
	env->DeleteLocalRef(jendTime);
	env->DeleteLocalRef(jtotalTime);
	jobject latsObject = env->NewObject(arrayListClass, struct_ArrayList);
	int count = 0;
	while (getline(fis, s)) {
		if (stop)
			return false;
		count++;
		if (count == 1250) {
			env->CallVoidMethod(thiz, progress_changed, (jlong) fis.tellg());
			count = 0;
		}
		jobject myLatLng2 = env->NewObject(myLatLng2Class, struct_MyLatLng2);
		env->SetDoubleField(myLatLng2, mLatitude, (jdouble)atof(s.c_str()));
		getline(fis,s);
		env->SetDoubleField(myLatLng2, mLongitude, (jdouble)atof(s.c_str()));
		getline(fis,s);
		env->SetFloatField(myLatLng2, mAltitude, (jfloat)atof(s.c_str()));
		getline(fis,s);
		jstring time = env->NewStringUTF(s.c_str());
		env->SetObjectField(myLatLng2, mTime, time);
		env->CallBooleanMethod(latsObject, arrayList_add, myLatLng2);
		env->DeleteLocalRef(time);
		env->DeleteLocalRef(myLatLng2);
	}
	fis.close();
	env->SetObjectField(cache, mLats, latsObject);
	env->DeleteLocalRef(latsObject);
	return true;
}
JNIEXPORT void Java_com_yupog2003_tripdiary_data_GpxAnalyzer2_stop(JNIEnv* env, jobject thiz) {
	stop = true;
}
}
