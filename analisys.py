import pandas as pd
import numpy as np
from scipy.stats import mannwhitneyu
import matplotlib.pyplot as plt
import sys
import matplotlib.pyplot as plt
import screeninfo
def statisticalAnalysis(compTuple1,compTuple2,dfResults):
  print("\n")
  #tuple = (name,data)
  # Assuming your CSV file has columns named 'data1' and 'data2', you can access these columns like this:
  name1 = compTuple1[0]
  name2 = compTuple2[0]
  data1 = compTuple1[1]['time']
  data2 = compTuple2[1]['time']
  # Calculate mean and standard deviation
  mean_data1 = np.mean(data1)
  std_data1 = np.std(data1)
  mean_data2 = np.mean(data2)
  std_data2 = np.std(data2)

  # Print the results
  print(name1+': mean=%.3f stdv=%.3f' % (mean_data1, std_data1))
  print(name2+': mean=%.3f stdv=%.3f' % (mean_data2, std_data2))
  # compare samples
  stat, p = mannwhitneyu(data1, data2)
  print('Statistics=%.3f, p=%.3f' % (stat, p))
  # interpret
  alpha = 0.05
  if p > alpha:
    row_series = pd.Series((name1, name2, round(p, 5), stat, False,"",""), index=columns)
    if name1 == name2:
       row_series = pd.Series((name1, name2, round(p, 5), stat, False,round(mean_data1, 5),round(std_data1, 5)), index=columns)
    dfResults = pd.concat([dfResults, row_series.to_frame().T], ignore_index=False)
    print('Same distribution (fail to reject H0)') 
  else:
    row_series = pd.Series((name1, name2, round(p, 5), stat, True,"",""), index=columns)
    if name1 == name2:
       row_series = pd.Series((name1, name2, round(p, 5), stat, False,round(mean_data1, 5),round(std_data1, 5)), index=columns)
    dfResults = pd.concat([dfResults, row_series.to_frame().T], ignore_index=True)
    print('Different distribution (reject H0) ' + name2 + ' has a relevant difference between distributions comparing to ' + name1)
  print(dfResults)
  print("\n")
  return dfResults  # Return the modified DataFrame



# Load data from CSV file
filepaths = ['KnapsackGA','KnapsackAlmostFullyParallel','KnapsackFullyParallel',
             'KnapsackFullyParallelSplitPop','KnapsackGAPartiallyParallel']

dataframes = []
for path in filepaths:
  
  if  len(sys.argv) > 1 and sys.argv[1] == "4" and path != 'KnapsackGA':
     path = path + '_4C.csv'
  else:
      path = path + '.csv'
  print(path)
  df = pd.read_csv(path)
  dataframes.append(df)

columns = ['Algorithm', 'ComparedAlgorithm', 'p', 'stat', 'StatisticalDifference','Mean','Stdv']
dfResults = pd.DataFrame(columns=columns)
for i in range(len(dataframes)):
   for j in range(len(dataframes)):
    dfResults = statisticalAnalysis((filepaths[i], dataframes[i]), (filepaths[j], dataframes[j]), dfResults)

print(dfResults)


# Get the screen width and height
screen = screeninfo.get_monitors()[0]  # Assuming the first monitor
screen_width = screen.width

# Calculate the figure size with a specified aspect ratio (e.g., 16:9)
aspect_ratio = 16 / 9
fig_width = (screen_width - 200) / 100 # Inches
fig_height = fig_width / aspect_ratio

# Create a figure with the calculated size
fig, ax = plt.subplots(figsize=(fig_width, fig_height))


transposed_data = [df['time'] for df in dataframes[1:]]

# Create a boxplot with labels on the Y-axis and values on the X-axis
ax.boxplot(transposed_data, vert=False, labels=filepaths[1:], whis=1.5)  # Adjust the value as needed

ax.set_xlabel('Time(s)')
ax.set_ylabel('Algoritmh')
ax.set_title('Parallel Algoritmh Analisys')
plt.subplots_adjust(left=0.3)
if len(sys.argv) > 1 and sys.argv[1] == "4":
    plt.savefig('boxplot_4C.png', dpi=300, bbox_inches='tight')
    # Define the CSV file name
    csv_file = 'StatResults_4C.csv'

    # Write the DataFrame to a CSV file
    dfResults.to_csv(csv_file, header=True, index=False)
else:
    plt.savefig('boxplot_8C.png', dpi=300, bbox_inches='tight')
    csv_file = 'StatResults_8C.csv'
    dfResults.to_csv(csv_file, header=True, index=False)

# Show the plot
#plt.show()
