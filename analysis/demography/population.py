#!/usr/bin/python3
'''
This script takes an Ecologia log file as its input, extracts the population
levels at each update and outputs this as a .csv file which can then be converted
to .ods or further analysed with an R script.

Written for Ecologia 1.0

@author Daniel Vedder
@date 6/4/2015
'''

import os
import sys

global version
global update_count
global generation_count
global carnivore_count
global herbivore_count
global grass_density
global src_file
global out_file

version = "0.3.1"


'''
Load the Ecologia log file, extracting the interesting lines.
'''
def load_log():
    global src_file
    global update_count, generation_count
    global herbivore_count, carnivore_count, grass_density
    try:
        log_file = open(src_file, "r")
        line = log_file.readline()
        while line:
            if "Executing update" in line:
                next_count = line[line.find("Executing update ")+17:-1]
                update_count.append(int(next_count))
            elif "Herbivore count" in line:
                next_count = line[line.find("Herbivore count: ")+17:-1]
                herbivore_count.append(int(next_count))
            elif "Carnivore count" in line:
                next_count = line[line.find("Carnivore count: ")+17:-1]
                carnivore_count.append(int(next_count))
            elif "Average grass density: " in line:
                next_count = line[line.find("density: ")+9:-2]
                grass_density.append(next_count)
            elif "Generation counter: " in line:
                next_count = line[line.find("counter: ")+9:-1]
                generation_count.append(next_count)
            line = log_file.readline()
        log_file.close()
    except IOError:
        print("Reading file '"+src_file+"' failed!")
        sys.exit()
            
'''
If we have the data of long runs, we need to compact them or they won't fit onto
a single spreadsheet. (LibreOffice can only deal with ~1000 data points.)
'''
def compact():
    global update_count, generation_count
    global herbivore_count, carnivore_count, grass_density
    if len(update_count) < 1000:
        return
    new_update_count = [update_count[0]]
    new_herbivore_count = [herbivore_count[0]]
    new_carnivore_count = [carnivore_count[0]]
    new_grass_density = [grass_density[0]]
    new_generation_count = [generation_count[0]]
    for i in range(1, len(update_count)):
        if (i+1)%10 == 0:
            new_update_count.append(update_count[i])
            new_herbivore_count.append(herbivore_count[i])
            new_carnivore_count.append(carnivore_count[i])
            new_grass_density.append(grass_density[i])
            new_generation_count.append(generation_count[i])
    update_count = new_update_count
    herbivore_count = new_herbivore_count
    carnivore_count = new_carnivore_count
    grass_density = new_grass_density
    generation_count = new_generation_count
    compact() #Keep going until the data set is small enough

'''
Save the accumulated data to a .csv file
'''
def save_csv():
    global out_file
    global update_count, generation_count
    global herbivore_count, carnivore_count, grass_density
    #Accumulate all the data
    data = "Updates"
    for u in update_count:
        data = data+","+str(u)
    data = data+"\nHerbivores"
    for h in herbivore_count:
        data = data+","+str(h)
    data = data+"\nCarnivores"
    for c in carnivore_count:
        data = data+","+str(c)
    data = data+"\nGrass density"
    for g in grass_density:
        data = data+","+str(g)
    data = data+"\nGenerations"
    for n in generation_count:
        data = data+","+str(n)
    #Then write it to file
    try:
        csv_file = open(out_file, "w")
        csv_file.write(data)
        csv_file.close()
    except IOError:
        print("Writing "+out_file+" failed!")
        sys.exit()

'''
Save the data into a table (.txt) for R
'''
def save_table():
    global out_file
    global update_count, generation_count
    global herbivore_count, carnivore_count, grass_density
    #Accumulate the data
    data = "Updates\tHerbivores\tCarnivores\tGrassDensity\tGenerations"
    for i in range(len(update_count)):
        data = data+"\n"+str(update_count[i])+"\t"+str(herbivore_count[i])+"\t"+str(carnivore_count[i])
        data = data+"\t"+str(grass_density[i])+"\t"+str(generation_count[i])
    #Then write it to file
    try:
        table_file = open(out_file, "w")
        table_file.write(data)
        table_file.close()
    except IOError:
        print("Writing "+out_file+" failed!")
        sys.exit()

'''
Print a help text
'''
def print_help():
    help_text = """Ecologia population tracker, version """+version+"""

Commandline parameters:
    --version -v		Show the version number and exit
	--help -h 			Show this help text and exit
	
	--logfile <file>	Use <file> as the input logfile
	--outfile <file>	Output to <file>
	--table	  			Output data as a table (.txt)
	--csv				Output data as a csv file
	--ods			Convert the data to ods via csv
	--compact		Compact large data sets (automatic for ods)"""
    print(help_text)

def main():
    global src_file
    global out_file
    global update_count, generation_count
    global carnivore_count, herbivore_count, grass_density
    src_file = "ecologia.log"
    #Choose the right default output file name
    if "--table" in sys.argv: out_file = "populations.txt"
    elif "--csv" in sys.argv or "--ods" in sys.argv: out_file = "populations.csv"
    else: raise Exception("Invalid file type specified!")
    #Let the user override default options
    if "--logfile" in sys.argv:
        src_file = sys.argv[sys.argv.index("--logfile")+1]
    if "--out-file" in sys.argv:
        out_file = sys.argv[sys.argv.index("--out-file")+1]
        #The chosen file ending determines the output type
        if out_file[-4:] == ".txt":
            sys.argv.append("--table")
        elif out_file[-4:] == ".csv" or outfile[-4:] == ".ods":
            sys.argv.append("--csv")
        #XXX This might give a conflict with the section above
        if "--table" in sys.argv and out_file[-4:] != ".txt":
            out_file = out_file+".txt"
        elif "--csv" in out_file[-4:] != ".csv":
            out_file = out_file+".csv"
    #Initialise variables
    update_count, generation_count = [], []
    carnivore_count, herbivore_count, grass_density = [], [], []
    #Do the actual work
    load_log()
    if "--compact" in sys.argv or "--ods" in sys.argv: compact()
    if "--table" in sys.argv: save_table()
    elif "--csv" in sys.argv or "--ods" in sys.argv:
        save_csv()
        if "--ods" in sys.argv:
            os.system("libreoffice --headless --convert-to ods "+out_file)

if __name__ == "__main__":
    if "--version" in sys.argv or "-v" in sys.argv:
        print("Ecologia population tracker, version "+version)
        sys.exit()
    elif "--help" in sys.argv or "-h" in sys.argv:
        print_help()
        sys.exit()
    else:
        main()
