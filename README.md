* This tool will convert the .graphml network you an extract from Facebook using the NameGenWeb FB App into Pajek (http://pajek.imfm.si/doku.php) readable files
*
* Instruction:
*
* Gather the FB Data using the NameGenWeb Facebook App (https://apps.facebook.com/namegenweb/) provided by the University of Oxford
* Store the GraphML file into a folder
* rename the *.graphml to profile.graphml
* Copy the FBGraphML2Pajek.jar into the folder and run it
** Windows: Double-Click the jar
** Linux/Mac: Open command line and execute the jar using java -jar FBGrahpML2Pajek.jar
** If you do not want real names in the file, execute the jar using: java -jar FBGrahpML2Pajek.jar true
* Pajek files should be created in the same folder
** .net - General network information (vertices and lines)
** .clu (3x) - Gender, locale and relation-ship status information for vertices
** .vec (2x) - number of likes, number of all friends for vertices
** log - information about different generated files
* Read the log
*
* Credits:
*
* Special thanks to Vladimir Batagelj and Andrej Mrvar for creating Pajek.
* Special thanks to the Oxford Internet Institute , Univeristy of Oxford for providing the NameGenWeb Facebook Application