
locoGP is a Genetic Programming system which modifies, compiles and executes Java source code with a focus on improving the performance of programs as measured in bytecodes executed. 

For more information, have a look at the locoGP paper
  https://www.scss.tcd.ie/~codykenb/locoGP.html
or drop me a line with any comments/questions
  codykenb at tcd.ie

----------------------------------------------------------------

Dependencies:

  Java 6 or 7 with -noverify

----------------------------------------------------------------

Directories:

  GP_Master      - Helper scripts for Master/Slave experiment workflow including graphing
                   Used as a working dir for deploying self-contained locoGP experiment jars
		   
  locoGP_eclipse - Eclipse project dir (use File -> Import)
                   Includes extra libraries required for locoGP/bycounter

----------------------------------------------------------------

Notes:

Change memory options depending on test problem and available memory
 - In Eclipse -> Run Configurations -> Arguments -> VM arguments
   add -Xss1000M (adjust as needed)
   see GP_Master/88-runJarInNewDir.sh for examples

Performance (bytecodes executed) measured with bycounter 
 - source available: https://sdqweb.ipd.kit.edu/wiki/ByCounter

Developed mainly using openjdk-6-jdk, Eclipse 3.8.1, Debian wheezy & jessie

----------------------------------------------------------------


