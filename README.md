## Welcome to reindex application for Elasticsearch
This application will help you to reindex one or more existing indices, into the local or remote Elasticsearch cluster.

#### The reindex destinations:
- Merge multiple indices into one index
- Reindex to alias
- Reindex to the index named with prefix + original_index_name
- Reindex to the index named with original_index_name + suffix
- Reindex to ILM rollover alias with the possibility to create first ILM index
  (for example index-000001 for size rollover or %3CINDEX_NAME-%7Bnow%2Fd%7D-000001%3E for time series indices)
- Remote reindex with the same index name

#### Features:
- Reindex support SSL with a certificate (you'll need to upload it), or without (we will set the SSL verification to "none")
- Reindex doesn't save your passwords. In case of the restart the application you'll need to insert them again.

- Reindex work with two reindex algorithms:
    1. Whole index algorithm - regular reindex, that reindex index as is, with the one reindex request
    2. Time oriented algorithm - each index will be splatted into several time frames, and each time frame will be run, as a separated reindex request ("reindex by query" with the "range" query). You have to define the time frame - the period of the data chunk in minutes, the field, that will be used as a date field and format of this field

- You can set up the number of concurrently processed indices, and the number of concurrent data frames per index (in case of Time oriented algorithm usage)

### Requirements
- Java 8+

- For remote reindex, you'll need to define the reindex.whitelist setting  in your elasticsearch.yml file in each Elasticsearch node of the remote cluster, you'll require to define:
  reindex.remote.whitelist: "otherhost:9200, another:9200, 127.0.10.*:9200, localhost:*"  (PAY ATTENTION! There is no http/https at start of the address)

- For remote reindex to cluster with https connection:
  In your elasticsearch.yml file in each Elasticsearch node of the remote cluster you'll require to define:
  reindex.remote.whitelist: "otherhost:9200, another:9200, 127.0.10.*:9200, localhost:*"  (PAY ATTENTION! There is no http/https at start of the address)
  reindex.ssl.verification_mode: none  (One of full, none, certificate)
  reindex.ssl.certificate: /etc/elasticsearch/certs/ca.crt (Path to root ca)
  This is only example... you can use another SSL settings

### Installation
This application works via a web browser, so you could use it on the local or remote machine

### Installation steps
Unpack the reindex.zip file in your local or remote machine

#### For the Windows
1. Edit file config/reindex.yml
   Specify the host and port
   host: HOST_OR_IP
   port: PORT

2. Run file reindex.bat

#### For the Linux

1. From the application home folder:
   chmod +x reindex.sh

2. Edit file config/reindex.yml
   Specify the host and port
   host: HOST_OR_IP
   port: PORT

3. ./reindex.sh

### Known issues
1. The application tested only in the Chrome browser
2. In Windows, in the time of application running, if you click within the console, it will pause the output.
   This could happen, because of  QuickEdit Mode and/or Insert options are checked in the console settings.
   To get to these settings, right-click on the PowerShell/Console-Logo in the top-left of your terminal window, then select 'Properties' 

### Know Bugs:

### Terms and conditions
This is the first Beta version, so if you'll find any bugs, or have any recommendations, please feel free to contact as in mail: support@dbeast.co

Copyright © 2020 Matskeplishvili Vakhtang & Yevgeny Levin
Licensed under the Apache License, Version 2.0