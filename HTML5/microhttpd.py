#!/usr/bin/env python
# -*- coding: utf-8 -*-

import os
import sys
import getopt

import BaseHTTPServer
import CGIHTTPServer

#---- Defaults
port = "8080"
basedir = "www/"
#----

#----------------------------------------
def usage():
    print "Uso: microhttpd -h -p port"
    print "     -h         Muestra este mensaje"
    print "     -p port    Sirve en el puerto indicado (def={0})".format(port)
    print "     -d dirname Sirve el contenido del directorio indicado (def={0})".format(basedir)

#----------------------------------------

try:
    opts, args = getopt.getopt(sys.argv[1:], "hp:d:", ["help", "port=", "dir="])
except getopt.GetoptError:
    usage()
    sys.exit(2)
    
for o, a in opts:
    if o in ("-h", "--help"):
        usage()
        sys.exit()
    if o in ("-p", "--port"):
        port = a
    if o in ("-d", "--dir"):
	basedir = a
        
if (port == None):
    usage()
    sys.exit()

try:
    address = ('', int(port))
except ValueError:
    usage()
    sys.exit(2)
    
os.chdir(basedir)
httpd = BaseHTTPServer.HTTPServer(address,
                                  CGIHTTPServer.CGIHTTPRequestHandler)
httpd.serve_forever()
