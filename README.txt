
locoGP is a Genetic Programming system which modifies, compiles and executes Java source code with a focus on improving the performance of programs as measured in bytecodes executed. 

For more information, have a look at the locoGP paper
  https://www.scss.tcd.ie/~codykenb/locoGP.html
or drop me a line with any comments/questions
  codykenb at tcd.ie

----------------------------------------------------------------

Dependencies:
  bycounter https://sdqweb.ipd.kit.edu/wiki/ByCounter
  openjdk-6-jdk (Java 6)

----------------------------------------------------------------

Directories:
  GP - Helper scripts for deployment across several machines and graphing
       Used as a working dir for running deploying self-contained locoGP experiment jars
  locoGP_lib - Libraries required for locoGP/bycounter

----------------------------------------------------------------

To configure locoGP in Eclipse:

1. Create a project by importing the src directory (call it locoGP). 

2. Add the locoGP_lib directory to the project "Referenced Libraries" (through the build path of the project)

3. Get a copy of the bycounter code https://sdqweb.ipd.kit.edu/wiki/ByCounter
   (r26653 is known good at time of writing)
   https://svnserver.informatik.kit.edu/i43/svn/code/BySuite/ByCounter/trunk/Palladio.BySuite.ByCounter/?p=26653

4. Create another project from the bycounter directory

5. Add the bycounter project to the build path of the locoGP project.

6. In Run Configurations -> Arguments -> VM arguments
   add -Xss1000M (adjust as needed)

7. Compile, run

