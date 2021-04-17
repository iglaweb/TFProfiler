/* 
 * File:   Log.h
 * Author: Alberto Lepe <dev@alepe.com>
 *
 * Created on December 1, 2015, 6:00 PM
 */

#ifndef LOG_H
#define LOG_H

#include <iostream>

using namespace std;

enum typelog {
    T_DEBUG,
    T_INFO,
    T_WARN,
    T_ERROR,
    T_FATAL
};

struct structlog {
    bool headers = false;
    typelog level = T_WARN;
};

structlog LOGCFG;

class LOG {
public:
    LOG() {}
    LOG(typelog type) {
        msglevel = type;
        if(LOGCFG.headers) {
            operator << ("["+getLabel(type)+"]");
        }
    }
    ~LOG() {
        if(opened) {
            cout << endl;
        }
        opened = false;
    }
    template<class T>
    LOG &operator<<(const T &msg) {
        if(msglevel >= LOGCFG.level) {
            cout << msg;
            opened = true;
        }
        return *this;
    }
private:
    bool opened = false;
    typelog msglevel = T_DEBUG;
    inline string getLabel(typelog type) {
        string label;
        switch(type) {
            case T_DEBUG: label = "DEBUG"; break;
            case T_INFO:  label = "INFO "; break;
            case T_WARN:  label = "WARN "; break;
            case T_ERROR: label = "ERROR"; break;
            case T_FATAL: label = "FATAL"; break;
        }
        return label;
    }
};

#endif  /* LOG_H */
