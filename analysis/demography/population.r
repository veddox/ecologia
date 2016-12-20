# Create two line graphs depicting population change over time from Ecologia 
# run data. (Requires a table file as created by population.py)
#
# version 0.1
# (c) 2016 Daniel Vedder

# The first plot uses linear axes for quantitative analyses.

d = read.table("populations.txt", header=T, sep="\t")

generations = d$Generations[length(d$Updates)]

jpeg(filename="populations-absolute.jpg", width=1440, height=960, pointsize=16)

par(mfrow=c(3, 1))

plot(d$Updates, d$Herbivores, type="l", main="Herbivore population development",
     xlab="Updates", ylab="Population size", col="blue")

plot(d$Updates, d$Carnivores, type="l", main="Carnivore population development",
     xlab="Updates", ylab="Population size", col="red")

plot(d$Updates, d$GrassDensity, type="l", main="Grass density development",
     xlab="Updates", ylab="Average grass density per tile in %", col="green")

dev.off()

# The second plot shows all three data series on a single graph with a
# logarithmic y-axis for qualitative comparisons.

jpeg(filename="populations-compare.jpg", width=1920, height=640, pointsize=20)

#In the following I need to add 1 to each data series because log(0) is not defined

plot(d$Updates, log(d$Herbivores+1), type="l", lwd=2, main=paste("Run over", generations, "generations"),
     xlab="Updates", ylab="Logarithmic population density", yaxt="n", col="blue")

par(new=T)

plot(d$Updates, log(d$Carnivores+1), type="l", yaxt="n", ylab="", xlab="",
     lwd=2, col="red")

par(new=T)

plot(d$Updates, log(d$GrassDensity+1), type="l", yaxt="n", ylab="", xlab="",
     lwd=2, lty=3, col="green")

dev.off()
