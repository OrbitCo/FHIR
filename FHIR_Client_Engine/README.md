# FHIR_Client_Engine

Modular FHIR Client application for creating and managing various clients interacting with FHIR repositories.<br>
Installing Node.js and Angular  application <br>
1.	Download and install https://nodejs.org/en/ <br>
2.	Run npm -version to verify version 8 <br>
  
3.	Follow instruction in this spec https://git.bcbssc.com/pages/InformationSystems/eltp-schedule/node/2020/06/25/nodejs.html  <br>
4.	 to set up GIT Bash and access to BCBS Node.js Artifactory <br>

5.	From GIT Bash run : export NODE_OPTIONS=--openssl-legacy-provider <br>
6.	Install Angular dependencies from /frontend run npm i
7.	Start app by running npm start 
8.	Start backend mvn spring-boot:run
9.	Verify that app is running: http://localhost:4200

## WAR Packgaging
The FHIR Client Engine expects to run as a contained war, but to configure it correctly, 
make sure to modify frontend/src/index.html:16 to the proper base URL, as well as 
frontend/src/enviornments/environment.prod.ts.  
1. cd into frontend, run `npm run build --omit=dev`
2. cd back to the root, run `mvn clean package`
3. The output war will be in backend/target/backend-0.0.1.war
