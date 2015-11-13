FROM cannin/rserve 

RUN sudo su - -c "R -e \"install.packages('gstat', repos='http://cran.r-project.org')\"" 
RUN sudo su - -c "R -e \"install.packages('cluster', repos='http://cran.r-project.org')\"" 
RUN sudo su - -c "R -e \"install.packages('mclust', repos='http://cran.r-project.org')\"" 
RUN sudo su - -c "R -e \"install.packages('ape', repos='http://cran.r-project.org')\"" 
RUN sudo su - -c "R -e \"install.packages('ncf', repos='http://cran.r-project.org')\"" 
RUN sudo su - -c "R -e \"install.packages('sp', repos='http://cran.r-project.org')\"" 
RUN sudo su - -c "R -e \"install.packages('forecast', repos='http://cran.r-project.org')\"" 

WORKDIR /code

RUN mkdir -p /var/www/html/LindaAnalytics

ENV WILDFLY_VERSION=9.0.1.Final
ENV JAVA_HOME=/usr/lib/jvm/java-7-openjdk-amd64                                                                                                                                                                                                       
ENV JBOSS_HOME=/code/wildfly

RUN apt-get update -y   

RUN apt-get install openjdk-7-jdk -y 

RUN apt-get install sed -y 

RUN cd /code    

RUN curl -O https://download.jboss.org/wildfly/$WILDFLY_VERSION/wildfly-$WILDFLY_VERSION.tar.gz     


RUN tar xf wildfly-$WILDFLY_VERSION.tar.gz     

RUN mv /code/wildfly-$WILDFLY_VERSION $JBOSS_HOME  

#RUN rm /code/wildfly-$WILDFLY_VERSION.tar.gz 

#RUN groupadd -r jboss -g 1001 && useradd -u 1001 -r -g jboss -m -d /code -s /sbin/nologin -c "JBoss user" jboss &&     chmod 755 /code  

RUN  sed -i 's|jboss.http.port:8080|jboss.http.port:8181|' wildfly/standalone/configuration/standalone.xml

ADD configuration/lindaAnalytics/ /code/wildfly/modules/lindaAnalytics
ADD target/RESTfulLINDA.war /code/wildfly/standalone/deployments/
ADD startRserve.R /code/

user root

CMD Rscript startRserve.R && wildfly/bin/standalone.sh
                    
#build docker
#docker build -t lindanalytics .

#run docker
#docker run --net=host -p 8181:8181  -v /var/www/html/LindaAnalytics:/var/www/html/LindaAnalytics lindanalytics


 
