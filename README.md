# A Java ACE HTTP server with gherkin auto-completion added.

Starts with the URL: http://localhost:8000/ace-builds-master/demo/autocompletion.html

Or: http://localhost:8000/ace-builds-master/demo/autocompletion.html?filename=t1.feature
Or: http://localhost:8000/ace-builds-master/demo/autocompletion.html?filename=t2.feature
etc...


This will stop the server: http://localhost:8000/stop

* ace-builds-master/gherkin-steps.js is read to find the step matches.
    It can be read from the current directory, src/main/resources, target/ or finally the resources

* If the system property "com.replicanet.ACEServer.debug.requests" is set then the URLs requests are displayed

ACE Build obtained from: https://github.com/ajaxorg/ace-builds
Version 26.04.15: https://github.com/ajaxorg/ace-builds/commit/9d12839819e8a8b90f4a0aba382cac9a273b5983


## Entry points

### com.replicanet.ACEServer

* public static void startServer(InetSocketAddress listenAddress) throws IOException

    startServer(new InetSocketAddress(8000));

* public static void stopServer() throws IOException
