## LinDA Analytics

> **Main Purpose:**
Enable the execution of conventional analytic processes against linked data
> **Features:**
> - Select and execute an analytic processes against a specific SPARQL query, through user-friendly interfaces with pre-configured parameters for specific algorithms.
> - Integration of Weka and R analytic algorithms. Support for J48, M5P, Apriori, LinearRegression, Arima,Morans I, Kriging, NCF correlogram, ClustersNumber, Kmeans, Ward Hierarchical Agglomerative, Model Based Clustering, LinearRegression in R algorithms
> - Ontology for mapping information relevant to the analytic process and the input and output sources.
> - Support input and output of analytic results in RDF format (N3, Turtle, RDF / XML).
> - Support tracking of analytics processes executed per user 
> - Interconnection with the visualization components for visualising the analytics’ output.
> - Ability to save / load analytic processes per user
> - List saved analytic processes
> - Ability to load datasets / training datasets in CSV or ARFF format
> - Re-evaluate the analytic process keeping the same trained model 
> - Before re-evaluation the analytics procsess , the user can a) change the output format, b) change the evaluation query c) Change the parameters of the current algorithm d) search within his analytics, e) Automatic refill of the analytic process description
> - User feedback on tool usage: a) queries participation on analytics, b) analytics effectiveness, c) data quality and output reuse, d) performance time  of each analytic process.


####Step 1 Install docker to server & git clone RESTfulLINDA code

####Step 2 go in RESTfulLINDA directory and execute the following commands

```
#build lindanalytics code
mvn clean install
 
#build docker container
docker build -t lindanalytics .

#run docker container
docker run --net=host -p 8181:8181  -v /var/www/html/LindaAnalytics:/var/www/html/LindaAnalytics lindanalytics
``` 

####For properties configuration...

> - you should go to properties module in RESTfulLINDA/configuration/lindaAnalytics/configuration/main/RESTfulLINDA.properties 

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


Your wildfly is now up and running in localhost:8181

If you wand to install analytics from scracth please follow the READMEold file

