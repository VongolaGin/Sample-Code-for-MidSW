# Sample-Code-for-MidSW
This repository contains some java classes for show a fragment of how I code

**Summary**

The classes on this sample are components for a upload worked files module that I code, and the function of this module
is to allow a user to upload excel format files with well-structured information, validate and process these. so after then, upload
the information on a massive and fast way to the database for purification data process calling a stored procedure.

**Challenge**

Process big data in a fast way and process them.

**That I did**

Defined the next elements:

* GUI for upload the files (_carga-archivo-trabajado.html_)
* Angular controller/service for call the necesary api endpoint (_carga-archivo-trabajado.controller.js_)

* Defined the endpoints (_ArchivoTrabajadoResource_)
* Defined the controller which will be calling and controlling the process flow. (_Archivo_TrabajadoServiceImpl_) 
* Defined a EnumClass which contains the Excel columns structure. (_ArchivoTrabajadoColumnsEnum_)
* Defined a Exception class which contains the possible exceptions from validations (_IscamExcelReaderException_)
* Defined a servive for process the file on a bussiness logic approach (_ArchivoTrabajadoReaderService_)
* Defined a service for phisyc files management (_ArchivoTrabajadoFileService_)
* Defined a service for DB calls (_ArchivoTrabajadoRepository_)

Implements the element on the controller with the next logic:

first, receive the worked files (well-structured excels) and the total excel files (a files with have inventary pieces and sales prices of products).
then, we validate the format files and if the users have uploaded the files before, if not, throws an exception (all knew exceptions have predefined messages).
after the validation, we process the totals excel file before the worked ones, because convenience.
The total excel process and the worked file process are the same, and consists on validate the number of columns, name of columns and the data types from the values.
the result from the validations results on a csv file with the same excel data, we needed to do that because the _Bulk Insert_ from Microsofts SQL Server 
just works with CSV and XML files.
when the totals csv and worked files csv's are generated we proceed to call the bulk insert on these but not before we apply a trash data analyse 
for prevent redundant data on the table target.
Then, we call a stored procedure to purify the uploaded data.
Finally, if all was OK, the files are stored and versioned on a directory which can be compressed and downloaded on a zip.

Note:
This codes will, obviously, do not work without the proyect context.
Also, note that the code wasn't modified, the java classas were just copied/pasted on the GitHub repository.
 
