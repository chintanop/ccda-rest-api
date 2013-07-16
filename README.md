CCDA REST API
=============

A CCDA REST API was developed to make it quick and super easy for developers to parse and retrieve information from CCDA documents.

Here is the [demo site](http://ccdapi.appliedinformaticsinc.com/client_demo)

API Operations
--------------


**POST /bbplus**

Add or update a patient record from a BlueButton+/CCDA file. 

*Parameters:*

Inputs:
       
 * bbfile: An XML string representation of the BlueButton+ file in the CCDA format. If the same patient data is posted at a later date, the API updates the stored record with the updated information. The uniqueness is determined based on the MRN/Institution data in the XML (see example below).

 * op_format: Specify output format "xml" or "json"


Output:

 * patient_id: An API-specific patient identifier token (string) that can be used to query further information for this patient.

 * status: The processing status of the XML ("processing", "completed", "failed"). 


**GET /bbplus/&lt;patient_id&gt;/&lt;section&gt;**

Get specific section of the patient record


*Parameters:*

Input:

 * patient_id: The unique identifier of the patient record returned by the API on the POST of the BlueButton+ file. 

 * section: The specific section to retrieve in the output. The section parameter can be any one of the following:
	1.	demographics
	2.	problems
	3.	medications
	4.	allergies
	5.	results
	6.	procedures [NotSupported]
	7.	encounters [NotSupported]
	8.	socialhistory [NotSupported]


 * op_format: Specify output format "xml" or "json"


Output:

 * status: The processing status of the XML ("processing", "completed", "failed"). 

 * patient_record: An XML or JSON representation of the requested section. The format of individual sections are described in the table.


* * *

Requirements
------------
- JDK 1.6
- Eclipse Juno release or higher
- Dependencies (included in the project)

 - MDHT Runtime 1.0.0.201302191803
 - Eclipse EMF Core 2.5
 - RESTlet
 - Gson 2.2.3


* * *

Installation/Set up
-------------------
1. Clone this repository onto your local
2. Import the directory as an Eclipse project. 
3. Export as a "Runnable Jar"
4. Run the application as java -jar ccda-parser-runnable.jar <port_number>


* * *

TODOs
-----
- Add parsers for currently not supported sections 
- Add a creators to support creating a CCDA document using the REST query
- Add authentication, rate throttling, logging capabilities


* * *

LICENSE
-------
Copyright (c) 2013 by Applied Informatics Inc. Licensed under the [Apache 2.0](http://www.apache.org/licenses/LICENSE-2.0.html) license.
