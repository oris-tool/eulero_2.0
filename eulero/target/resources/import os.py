import os
import pandas as pd
import matplotlib.pyplot as plt

def test_case_plot_handler(test_path_name, case):
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

        if "Heuristic1" in file:
            legendString = 'Heuristic1'
            colorString = 'blue'
            my_zorder = 15

        if "Heuristic2" in file:
            legendString = 'Heuristic2'
            colorString = 'aqua'
            my_zorder = 10

        if "Heuristic3" in file:
            legendString = 'Heuristic3'
            colorString = 'chartreuse'
            my_zorder = 5

        df = pd.read_csv(path + "/" + file, index_col = 0)

            ### Create line for every file
        plt.plot(df, label=legendString, color=colorString, linewidth=1.5, zorder=my_zorder)
    
    ### Generate the plot
    if case == "Te 1":
        plt.legend(loc='upper left', fontsize=16, ncol=5, bbox_to_anchor=(1.1, 1.05), handlelength=1)
    plt.xticks(fontsize=16)
    plt.yticks(fontsize=16)
    plt.title(label="Model " + str(case).replace("Test ", ""), x=0.125, y=1.0, pad=-20, fontsize=16, weight='bold')
    plt.grid()
    plt.savefig(path + "/" + str(case) + "-" + graphType + ".png", bbox_inches='tight')
    plt.close()


### MAIN ###
PATH = os.path.abspath(os.path.dirname(__file__)) + "/../../results/approximantAppendixResult/" # Use your path

# Stampo e memorizzo cdf da sola
fileNames = os.listdir(PATH)
### Filter file name list for files ending with .txt
fileNames = [file for file in fileNames if '.txt' in file]

for file in sorted(fileNames):
    if "function" in file:
        legendString = 'function'
        colorString = 'red'
        my_zorder = 20

    df = pd.read_csv(PATH + "/" + file, index_col = 0)

        ### Create line for every file
    plt.plot(df, label=legendString, color=colorString, linewidth=1.5, zorder=my_zorder)

### Generate the plot
plt.legend(loc='upper left', fontsize=16, ncol=5, bbox_to_anchor=(1.1, 1.05), handlelength=1)
plt.xticks(fontsize=16)
plt.yticks(fontsize=16)
plt.grid()
plt.savefig(PATH + "/function.png" bbox_inches='tight')
plt.close()
