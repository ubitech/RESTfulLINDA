## LinDA Analytics

Main Purpose
Enable the execution of conventional analytic processes against linked data
Features
Select and execute an analytic processes against a specific SPARQL query, through user-friendly interfaces with pre-configured parameters for specific algorithms.
Integration of Weka and R analytic algorithms. Support for J48, M5P, Apriori, LinearRegression, Arima,Morans I, Kriging, NCF correlogram, ClustersNumber, Kmeans, Ward Hierarchical Agglomerative, Model Based Clustering, LinearRegression in R algorithms
Ontology for mapping information relevant to the analytic process and the input and output sources.
Support input and output of analytic results in RDF format (N3, Turtle, RDF / XML).
Support tracking of analytics processes executed per user 
Interconnection with the visualization components for visualising the analytics’ output.
Ability to save / load analytic processes per user
List saved analytic processes
Ability to load datasets / training datasets in CSV or ARFF format
Re-evaluate the analytic process keeping the same trained model 
Before re-evaluation the analytics procsess , the user can a) change the output format, b) change the evaluation query c) Change the parameters of the current algorithm d) search within his analytics, e) Automatic refill of the analytic process description
User feedback on tool usage: a) queries participation on analytics, b) analytics effectiveness, c) data quality and output reuse, d) performance time  of each analytic process.


##STEP 1 --INSTALL WildFly SERVER -- 

###STEP 1.1 Download & Install WildFly 9 from http://wildfly.org/downloads/


Actual link:
http://download.jboss.org/wildfly/9.0.0.CR1/wildfly-9.0.0.CR1.zip

###STEP 1.2 -- add LindaAnalytics module to share/wildfly-9.0.0.CR1/modules
1.create in $WILDFLY_HOME/modules/ 3 folders one inside of the other as  "/lindaAnalytics/configuration/main/".

2.inside "main" folder create module.xml file as follows:
```
<?xml version="1.0" encoding="UTF-8"?>  
  <module xmlns="urn:jboss:module:1.1" name="lindaAnalytics.configuration">  
      <resources>  
	  <resource-root path="."/>  
      </resources>  
  </module>  
```  
3.inside "main" folder create RESTfulLINDA.properties file as follows (change adequately the properties):
```
# Database IP
dbip=127.0.0.1
#Database port
dbport=3306
#db username
username=?
#db password
password=?
#db name
dbname=?
rdf2anyServer=http://localhost:8081
lindaworkbenchURI= http://localhost:8000/
docroot= /home/eleni/IdeaProjects/LindaWorkbench/linda/
analyticsRepo = /var/www/LindaAnalytics/
```

###STEP 1.3 -- change http port of wildfly to 8181
Go to wildfly-9.0.0.CR1/standalone/configuration/standalone.xml and change the http port as follows:
```
<socket-binding name="http" port="${jboss.http.port:8181}"/>
```

###STEP 1.4 --DEPLOY LINDA ANALYTICS WAR TO WildFly SERVER 
Deploying from command line with WildFly.The following command deploys the application:
```
$WILDFLY_HOME/bin/jboss-cli.sh --connect --command="deploy --force [PATH_TO_RESTfulLINDA.war]"
```
Undeploying the application works adequately:
```
$WILDFLY_HOME/bin/jboss-cli.sh --connect --command="undeploy RESTfulLINDA.war"
```


###STEP 1.5 START locally WildFly SERVER
Go to   cd $WILDFLY_HOME/bin/ and execute:
```
nohup sudo ./standalone.sh 
```


###STEP 2: INSTALL R PROJECT

##STEP 2.1 : ADD CRAN REPOSITORY

//see ubuntu version
lsb_release -a
(for linda common server is trusty)

//Go To
sudo nano /etc/apt/sources.list 

//Add CRAN Repository
```
deb http://cran.r-project.org/bin/linux/ubuntu trusty/
```

//add keys
```
sudo apt-key adv --keyserver keyserver.ubuntu.com --recv-keys E084DAB9
sudo add-apt-repository ppa:marutter/rdev
```
```
sudo apt-get install r-base-core
```
Useful links: 
http://cran.r-project.org/bin/linux/ubuntu/README
http://numerorojo.wordpress.com/2008/04/27/instalar-r-en-ubuntu/


##STEP 2.2 : INSTALL JRI
```
sudo aptitude install r-cran-rjava
```
##STEP 2.3 : ADD environmental parameters

//add to bashrc
sudo nano /etc/bash.bashrc OR sudo nano /etc/bash.bashrc/etc/bash.bashrc
//the following environmental variables
```
  export R_HOME=/usr/lib/R/
  export CLASSPATH=.:/usr/lib/R/site-library/rJava/jri/
  export LD_LIBRARY_PATH=/usr/lib/R/site-library/rJava/jri/
```

//mv jars to CLASSPATH
  sudo cp /usr/lib/R/site-library/rJava/jri/libjri.so  /usr/lib/

Useful links: 
http://binfalse.de/2011/02/talking-r-through-java/
http://www.rforge.net/JRI/
http://blog.pingoured.fr/index.php?post/2009/03/23/Getting-rJava/JRI-to-work%3A


##STEP 2.4 : INSTALL extra R packages

(from command line you can install them by executing sudo R CMD INSTALL package_name 
for example
sudo R CMD INSTALL spdep_0.5-82.tar.gz 

sudo R CMD INSTALL Rserve_1.7-3.tar.gz
)

packages to install:

Rserve 
cluster
mclust
forecast
sp
gstat
ape
ncf


##STEP 2.5 : START Rserve Server
start Rserve from R console
```
>R
  >library(Rserve)
  >Rserve()
``` 
  
OR put  to  /etc/rc.local the following line:
```
sudo R CMD Rserve
```


  
  





