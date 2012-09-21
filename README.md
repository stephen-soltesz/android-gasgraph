Screenshots
================

  * [Screenshots](https://github.com/stephen-soltesz/android-gasgraph/wiki)

Setup
================

Follow the steps for installing Eclipse, and the Android development
environment within Eclipse.

   * https://developer.android.com/sdk/index.html

Download source:

   * git clone https://github.com/stephen-soltesz/android-gasgraph.git

Then, start Eclipse and import GasGraph into your workspace:

   * File -> New -> Android Project
   * Create Project from Existing Source
   * Choose directory android-gasgraph/GasGraph
   * Finish

There will be errors immediately related to missing dependencies.

GasGraph depends on two external libraires:

  * Charts4j -- for interaction with Google Charts api

      * https://code.google.com/p/charts4j/ 
      * developed using version 1.3

  * ORMLite -- for sqlite db generation and maintenance

    * http://ormlite.com
    * developed using version 4.33 of ormlite-android and ormlite-core

Download these libraries and add them to the build path.

  * File -> Properties -> Java Build Path -> Libraries
  * Click on the missing JAR files, and click Edit
  * Choose the location of appropriate jar in your file system

Now you should be able to build and run the source.
