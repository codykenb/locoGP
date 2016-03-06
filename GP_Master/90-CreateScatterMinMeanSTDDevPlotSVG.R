#library(ggplot2)
library(reshape2)
library('tikzDevice')

genFit <- read.table(file="allfitness.txt",header=FALSE, nrows=-1, fill = TRUE)

na.omit(genFit)

# plot(apply(genFit, 2, min), ylim=c(.85,1), xlim=c(0,40), xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")
# pdf('ScatterMinMeanStdDev.pdf')
tikz('Scatter.tex')
# plot(genFit$vals, xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)", xlim=c(0,100), ylim=c(0,300))

#plot(gen)
genFitLog=log10(genFit)
genFitMelt=melt(genFitLog)


plot.default(genFitMelt,  pch='.',  na.rm=TRUE, xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")

dev.off
##########################################################################
#stripchart(genFit)
tikz('Avg.tex')

plot(apply(genFit, 2, mean, na.rm=TRUE), xlab="Generation No.",ylab="Fitness (Normalised)", main="Mean Fitness Vs Generation (lower is better)", pch=4)
lines(apply(genFit, 2, mean, na.rm=TRUE))
# abline(lm(genFit))
dev.off
##########################################################################

#points(apply(genFit, 2, min, na.rm=TRUE), ylab="Fitness (Normalised)", main="Min Fitness Vs Generation (lower is better)")

#plot(apply(genFit, 2, min, na.rm=TRUE), xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")
# plot(genFit)
tikz('Min.tex')

plot(apply(genFit, 2, min, na.rm=TRUE), xlab="Generation No.",ylab="Fitness (Normalised)", main="Min Fitness Vs Generation (lower is better)")
#lines(apply(genFit, 2, min, na.rm=TRUE))

#png('ScatterMinMeanStdDev.png')
#plot(apply(genFit, 2, min, na.rm=TRUE), xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")
dev.off



# points(apply(genFit2, 2, min), pch=23)

#plot(apply(genFit, 2, max),ylim=c(.8,700),log="y")
#points(apply(genFit, 2, min))
#points(apply(genFit, 2, mean))
#points(apply(genFit, 2, mean), pch=22)
#plot(apply(genFit, 2, max),ylim=c(.8,700),log="y")
#points(apply(genFit, 2, mean), pch=22)
#points(apply(genFit, 2, min), pch=23)
#plot(apply(genFit, 2, min), pch=23)


# nice!
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_point()
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_point() +stat_smooth()
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_point() +stat_identity()
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_point(stat = "smooth")
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_point()
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_boxplot()
#ggplot(melt(log10(genFit)), aes(variable, value))+ geom_smooth()
#ggplot(melt(log10(genFit)), aes(variable, value, group =1))+ geom_smooth()
#ggplot(melt(log10(genFit)), aes(variable, value, group =2))+ geom_smooth()
#http://docs.ggplot2.org/0.9.3/geom_linerange.html