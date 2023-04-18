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
PATH = os.path.abspath(os.path.dirname(__file__)) + "/../../results/approximantAppendixResult" # Use your path

# Stampo e memorizzo cdf da sola
legendString = 'function'
colorString = 'red'
df = pd.read_csv(PATH + "/function.txt", index_col = 0)

    ### Create line for every file
plt.plot(df, label=legendString, color=colorString, linewidth=1.5)

### Generate the plot
plt.legend(loc='lower right', fontsize=16)
plt.xticks(fontsize=16)
plt.yticks(fontsize=16)
plt.grid()
plt.savefig(PATH + "/function.png", bbox_inches='tight')
plt.ylim(-0.1, 1.1)
plt.xlim(0,3)

plt.scatter(0.67, 0, label="d", marker="x")
plt.scatter(0.99, 0.4827075000000002, label="flection pt.", marker="o")
plt.scatter(1.3, 0.75, label="q3", marker="*")
plt.legend(loc='lower right', fontsize=16, ncol=1)
plt.savefig(PATH + "/function+scatters.png", bbox_inches='tight')

legendString = 'tangent line'
colorString = 'blue'
df2 = pd.read_csv(PATH + "/line.txt", index_col = 0)

    ### Create line for every file
plt.plot(df2, label=legendString, color=colorString, linewidth=0.8)
plt.legend(loc='lower right', fontsize=16, ncol=1)
#plt.savefig(PATH + "/function+scatters+line.png", bbox_inches='tight')

legendString = 'approximation'
colorString = 'green'
df3 = pd.read_csv(PATH + "/approximation.txt", index_col = 0)

    ### Create line for every file
plt.plot(df3, label=legendString, color=colorString, linewidth=1.5)
plt.legend(loc='lower right', fontsize=16, ncol=1)
plt.savefig(PATH + "/function+scatters+line+appr.png", bbox_inches='tight')  

plt.close()

'''legendString = 'function'
colorString = 'red'
df = pd.read_csv(PATH + "/Bis/function.txt", index_col = 0)

    ### Create line for every file
plt.plot(df, label=legendString, color=colorString, linewidth=1.5)
plt.plot(pd.read_csv(PATH + "/Bis/approximation.txt", index_col = 0), label="approximation", color="green", linewidth=1.5)

plt.scatter(1, 0, label="d", marker="x")
# plt.scatter(0.99, 0.4827075000000002, label="flection pt.", marker="o")
plt.scatter(2.594, 0.75, label="q3", marker="*")

### Generate the plot
plt.legend(loc='lower right', fontsize=16)
plt.xticks(fontsize=16)
plt.yticks(fontsize=16)
plt.grid()
plt.savefig(PATH + "/Bis/approximation.png", bbox_inches='tight')
plt.ylim(-0.1, 1.1)
plt.xlim(0,8)'''