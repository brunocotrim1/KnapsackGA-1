import pandas as pd
import numpy as np
from scipy.stats import mannwhitneyu
import matplotlib.pyplot as plt

def statisticalAnalysis(compTuple1,compTuple2):
  print("\n")
  #tuple = (name,data)
  # Assuming your CSV file has columns named 'data1' and 'data2', you can access these columns like this:
  name1 = compTuple1[0]
  name2 = compTuple1[0]
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
      print('Same distribution (fail to reject H0) ')
  else:
      print('Different distribution (reject H0) '+name2 + ' has a relevant differente between distributions comparing to '+ name2)
  print("\n")
# Load data from CSV file
filepaths = ['KnapsackGA.csv','KnapsackAlmostFullyParallel.csv','KnapsackFullyParallel.csv',
             'KnapsackFullyParallelSplitPop.csv','KnapsackGAPartiallyParallel.csv']


dataframes = []
for path in filepaths:
  df = pd.read_csv(path)
  dataframes.append(df)

for i in range(len(dataframes[1:])):
   statisticalAnalysis((filepaths[0],dataframes[0]),(filepaths[i],dataframes[i]))


import matplotlib.pyplot as plt
import screeninfo  # You may need to install this library

# Get the screen width and height
screen = screeninfo.get_monitors()[0]  # Assuming the first monitor
screen_width = screen.width

# Calculate the figure size with a specified aspect ratio (e.g., 16:9)
aspect_ratio = 16 / 9
fig_width = (screen_width - 200) / 100  # Convert screen width to inches (adjust as needed)
fig_height = fig_width / aspect_ratio

# Create a figure with the calculated size
fig, ax = plt.subplots(figsize=(fig_width, fig_height))

# Transpose data1 and data2 for plotting
transposed_data = [df['time'] for df in dataframes[1:]]

# Create a boxplot with labels on the Y-axis and values on the X-axis
ax.boxplot(transposed_data, vert=False, labels=filepaths[1:], whis=1.5)  # Adjust the value as needed

# Set labels and title
ax.set_xlabel('Time(s)')
ax.set_ylabel('Algoritmh')
ax.set_title('Parallel Algoritmh Analisys')

# Adjust the left margin or padding
plt.subplots_adjust(left=0.3)  # Adjust this value as needed
# Save the plot as a PNG file
plt.savefig('boxplot.png', dpi=300, bbox_inches='tight')  # Adjust the file name and DPI as needed

# Show the plot
#plt.show()
