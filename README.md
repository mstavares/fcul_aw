# fcul_aw
Web Apps Project


## Crawler

To run the web crawler:

  `mvn exec:java`

### Optional jar arguments:

* `-f` : force update every disease
* `-d [name]` : only update the disease with the given name
* `-s [amount]` : limit the amount of updated diseases to the given size


NOTE: We developed the project backend using the Spring framework for Java, so we generated the Jar and stored it in our group's home folder in the appserver. All the front end is deployed in 'appserver.alunos.di.fc.ul.pt/~aw006/project/'

The frontend is prepared to work with the backend running on port 8086, so we can run the jar with the command 'java -jar web.jar --server.port=8086'

Right now we aren't able to produce any requests even after running the jar because the firewall of the server is blocking any external requests to any port not in use, so we can't have the backend running and having it accessible by the frontend since the frontend uses ajax

This worked a few days ago but the firewall started blocking requests to the ports recently, so we weren't able to do anything else since we already had everything done to work with an external server.

Because of this, the frontend will appear fine but won't show any information, since we can't get it through our API.
