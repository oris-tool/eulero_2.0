from fileinput import filename
import os
from traceback import print_tb
from turtle import color
import pandas as pd
import matplotlib.pyplot as plt

def test_case_plot_handler(test_path_name, case):
    PDF_PATH = test_path_name #+ "/PDF"

    plot_from_filenames(PDF_PATH, case, case)


def plot_from_filenames(path, case, graphType):
    fileNames = os.listdir(path + "/" + graphType)
    ### Filter file name list for files ending with .txt
    fileNames = [file for file in fileNames if '.txt' in file ]

    for file in sorted(fileNames):
        linewidth = 1.5
        df = pd.read_csv(path + "/" + graphType + "/" + file, index_col = 0)

        if "runs" in file:
            legendString = 'GroundTruth'
            colorString = 'red'
            my_zorder = 8
            
        if "Simulation" in file:
            legendString = 'Simulation'
            colorString = 'orange'
            my_zorder = 0
            linewidth = 0.5

        if "Averaged" in file:
            legendString = 'Averaged'
            colorString = 'grey'
            my_zorder = 0
            linewidth = 0.5

        if "Heuristic 1" in file:
            legendString = 'Heuristic 1'
            colorString = 'blue'
            my_zorder = 15
            otherdf = pd.read_csv(path + "/" + graphType + "/" + file)
            xMaxInd = otherdf.iloc[:, 0].idxmax()
            xMinInd = otherdf.iloc[:, 0].idxmin()
            yMaxInd = otherdf.iloc[:, 1].idxmax()
            yMinInd = otherdf.iloc[:, 1].idxmin()

            xMin = otherdf.iloc[:, 0][xMinInd]
            xMax = otherdf.iloc[:, 0][xMaxInd]
            yMin = otherdf.iloc[:, 1][yMinInd]
            yMax = otherdf.iloc[:, 1][yMaxInd]

        if "Heuristic 1_intera" in file:
            legendString = 'Heuristic 1 - Doppia replicazione'
            colorString = 'green'
            my_zorder = 15
            otherdf = pd.read_csv(path + "/" + graphType + "/" + file)

        if "Heuristic 1 - SEA" in file:
            legendString = 'Heuristic 1'
            colorString = 'cyan'
            my_zorder = 15

        if "Heuristic 2" in file:
            legendString = 'Heuristic 2'
            colorString = 'chartreuse'
            my_zorder = 10

        ### Create line for every file
        plt.plot(df, label=legendString, color=colorString, linewidth=linewidth, zorder=my_zorder)
    
    ### Generate the plot
    #plt.legend(loc='lower left', fontsize=16, ncol=1, handlelength=1)#bbox_to_anchor=(1.1, 1.05),

    plt.xticks(fontsize=16)
    plt.yticks(fontsize=16)
    plt.ylim(-0.01, 5)
    plt.xlim(35, 44)

    plt.title(label="BOTTOM D6 B4 T4", x=0.25 , y=1.0, pad=-20 , fontsize=16, weight='bold')
    plt.grid() 
    plt.savefig(path + "/" + graphType + "/" + str(case) + ".png", bbox_inches='tight')
    plt.close()


### MAIN ###
### Set your path to the folder containing the .csv files, choosing the right test iteration
#validationPATH = os.path.abspath(os.path.dirname(__file__)) + "/../../results/JULERO/ValidationTest/" # Use your path
validationPATH = "/Users/riccardoreali/Desktop/eulero-workspace/experiments/results/JULERO/ComplexTest/BOTTOM/"
fileNames = [file for file in os.listdir(validationPATH) if os.path.isdir(validationPATH + file)]
for file in fileNames:
    if ".DS_Store" not in file:
        test_case_plot_handler(validationPATH + file, "PDF")
        test_case_plot_handler(validationPATH + file, "CDF")

"""plt.plot(range(10), label="Simulation", color="orange", linewidth=1.5, zorder=0)
plt.plot(range(10), label="Averaged Simulation", color="grey", linewidth=1.5, zorder=10)
plt.plot(range(10), label="Heuristic 1", color="blue", linewidth=1.5, zorder=15)
plt.plot(range(10), label="Heuristic 2", color="chartreuse", linewidth=1.5, zorder=20)
plt.legend(loc='lower left', fontsize=16, ncol=5, handlelength=1, bbox_to_anchor=(0.0, 1.))
plt.show()"""


"""pdf1path = "/Users/riccardoreali/Desktop/Downward/downCdf.txt"
pdf2path = "/Users/riccardoreali/Desktop/Downward/downCdfAppr.txt"
point = "/Users/riccardoreali/Desktop/Mixed/point.txt"


df1 = pd.read_csv(pdf1path, index_col = 0)
df2 = pd.read_csv(pdf2path, index_col = 0)




#plt.legend(loc='upper left', fontsize=15, ncol=1)
plt.xticks(fontsize=15)
plt.yticks(fontsize=15)
plt.xlim(-0.2, 4)
plt.ylim(-0.05, 1.05)
plt.grid()
plt.show()"""


"""path = '/Users/riccardoreali/Desktop/model_suite/'
firstFolders = os.listdir(path)
for firstFolder in firstFolders:
    secondFolders = os.listdir(path + firstFolder + "/")

    for secondFolder in secondFolders:
        pathToget = path + firstFolder + "/" + secondFolder + "/"

        if ".DS_Store" not in pathToget:
            df = pd.read_csv(pathToget + "PDF.txt", usecols = ['t', 'f'])
            xMax = df['t'].max()
            xMin = df['t'].min()
            yMax = df['f'].max()
            newDf = pd.read_csv(pathToget + "PDF.txt", index_col = 0)
            print(newDf)

            ### Create line for every file
            plt.plot(newDf, color='blue', linewidth=1.5)
                
            ### Generate the plot
            #plt.legend(loc='lower left', fontsize=16, ncol=1, handlelength=1)#bbox_to_anchor=(1.1, 1.05),

            plt.xticks(fontsize=16)
            plt.yticks(fontsize=16)
            plt.ylim(-0.1,yMax + 0.1)
            plt.xlim(xMin, xMax)

            plt.title(label="", x=0.2 , y=1.0, pad=-20 , fontsize=16, weight='bold')
            plt.grid() 
            plt.savefig(pathToget + "PDF_plot.png", bbox_inches='tight')
            plt.close()

path = '/Volumes/GoogleDrive/Il mio Drive/Unifi/Lavoro/Articoli/RTSOPS22/img/'
firstFolders = os.listdir(path)
for file in firstFolders:
    if("CDF" in file):
        if "Deadline" in file:
            legendString = 'Penalty'
            colorString = 'black'
            linewidth = 0.5
            
        if "Winner" in file:
            legendString = 'All. A'
            colorString = 'green'
            print("sto qui")
            linewidth = 1

        if "Failing" in file:
            legendString = 'All. C'
            colorString = 'red'
            linewidth = 1

        if "Intermediate" in file:
            legendString = 'All. B'
            colorString = 'blue'
            linewidth = 1

        df = pd.read_csv(path + file, index_col=0)
        ### Create line for every file
        plt.plot(df, label=legendString, color=colorString, linewidth=linewidth)

plt.xticks(fontsize=16)
plt.yticks(fontsize=16)
plt.ylim(-0.1,1.1)
plt.xlim(0, 15)

plt.title(label="", x=0.2 , y=1.0, pad=-20 , fontsize=16, weight='bold')
plt.grid()
plt.legend(loc='lower right', fontsize=12, ncol=1, handlelength=1, bbox_to_anchor=(1, 0.1))
#plt.show()
plt.savefig(path + "figure.png", bbox_inches='tight')
plt.close()"""