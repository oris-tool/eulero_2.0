import os
import pandas as pd
import matplotlib.pyplot as plt

"""def test_case_plot_handler(test_path_name, case):
    CDF_PATH = test_path_name + "/CDF"
    PDF_PATH = test_path_name + "/PDF"

    plot_from_filenames(CDF_PATH, case, "CDF")
    plot_from_filenames(PDF_PATH, case, "PDF")


def plot_from_filenames(path, case, graphType):
    fileNames = os.listdir(path)
    ### Filter file name list for files ending with .txt
    fileNames = [file for file in fileNames if '.txt' in file]

    for file in sorted(fileNames):
        if "GroundTruth" in file:
            legendString = 'GroundTruth'
            colorString = 'red'
            my_zorder = 20

        if "Simulation" in file:
            legendString = 'Simulation'
            colorString = 'orange'
            my_zorder = 0

        if "Heuristic 1" in file:
            legendString = 'Heuristic 1'
            colorString = 'blue'
            my_zorder = 15

        if "Heuristic 2" in file:
            legendString = 'Heuristic 2'
            colorString = 'aqua'
            my_zorder = 10

        if "Heuristic 3" in file:
            legendString = 'Heuristic 3'
            colorString = 'chartreuse'
            my_zorder = 5

        df = pd.read_csv(path + "/" + file, index_col = 0)

            ### Create line for every file
        plt.plot(df, label=legendString, color=colorString, linewidth=1.5, zorder=my_zorder)
    
    ### Generate the plot
    plt.legend(loc='lower left', fontsize=16, ncol=1, handlelength=1)#bbox_to_anchor=(1.1, 1.05),
    
    if case == "Test A":
        rightLim = 6
        leftLim = 2
        name = "Test 1a"
    if case == "Test B":
        rightLim = 7
        leftLim = 2
        name = "Test 1b"
    if case == "Test C":
        rightLim = 6
        leftLim = 2
        name = "Test 2a"
    if case == "Test D":
        rightLim = 6
        leftLim = 2
        name = "Test 2b"
    if case == "Test E":
        rightLim = 4
        leftLim = 0
        name = "Test 3a"
    if case == "Test F":
        rightLim = 5
        leftLim = 0
        name = "Test 3b"
    if case == "Test G":
        rightLim = 4
        leftLim = 1
        name = "Test 4a"
    if case == "Test H":
        leftLim = 1
        rightLim = 6
        name = "Test 4b"

    plt.xticks(fontsize=16)
    plt.yticks(fontsize=16)
    plt.xlim(leftLim,rightLim)
    plt.title(label="Model " + str(name).replace("Test ", ""), x=0.125, y=1.0, pad=-20, fontsize=16, weight='bold')
    plt.grid()
    plt.savefig(path + "/" + str(case) + "-" + graphType + ".png", bbox_inches='tight')
    plt.close()


### MAIN ###
### Set your path to the folder containing the .csv files, choosing the right test iteration
testCases = ["Test A", "Test B", "Test C", "Test D", "Test E", "Test F", "Test G", "Test H"]
PATH = os.path.abspath(os.path.dirname(__file__)) + "/../../results/AutomatedTest/ExpMixture/Forward_Uniform_DoubleEXP_C3_StoOrd/" # Use your path

'''for case in testCases:
    path = (PATH + case)
    test_case_plot_handler(path, case)'''

plt.plot(range(10), label="Ground Truth", color="red", linewidth=1.5, zorder=5)
#plt.plot(range(10), label="Simulation", color="orange", linewidth=0.5, zorder=0)
#plt.plot(range(10), label="Averaged Simulation", color="grey", linewidth=0.5, zorder=10)
plt.plot(range(10), label="Heuristic 1 - A1", color="blue", linewidth=1.5, zorder=15)
plt.plot(range(10), label="Heuristic 2 - A2", color="cyan", linewidth=1.5, zorder=20)
plt.legend(loc='lower left', fontsize=16, ncol=5, handlelength=1, bbox_to_anchor=(0.0, 1.))
plt.show()
plt.savefig('/Users/riccardoreali/legend.png', bbox_inches='tight')"""

df1 = pd.read_csv("/Users/riccardoreali/Desktop/CDF_Nominal.txt", index_col = 0)
df2 = pd.read_csv("/Users/riccardoreali/Desktop/CDF_Mean.txt", index_col = 0)
df3 = pd.read_csv("/Users/riccardoreali/Desktop/CDF_Median.txt", index_col = 0)

plt.plot(df1, label="nominal", color="red", linewidth=1.)
plt.plot(df2, label="mean", color="blue", linewidth=1.)
plt.plot(df3, label="median", color="green", linewidth=1.)
    
plt.legend(loc='lower right', fontsize=14, ncol=1, handlelength=1)#bbox_to_anchor=(1.1, 1.05),

plt.xticks(fontsize=14)
plt.yticks(fontsize=14)
plt.grid()
plt.show()

