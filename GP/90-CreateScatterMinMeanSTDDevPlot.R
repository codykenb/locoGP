#library(ggplot2)
library(reshape2)
library('tikzDevice')

genFit <- read.table(file="allfitness.txt",header=FALSE, nrows=-1, fill = TRUE)
na.omit(genFit)
genFitLog=log10(genFit)
genFitMelt=melt(genFitLog)
genFitOrig=melt(genFit)

########################################## Draw Scatter and mean graph
pdf('ScatterMinMeanStdDev.pdf',width=12,height=9)
plot.default(jitter(as.numeric(gsub("V","" ,genFitMelt$variable)),factor=1),jitter(genFitMelt$value),  pch='.',  na.rm=TRUE, xlab="Generation", ylab="Fitness (Log)",yaxt="n")
axis(2, at=round(unique(genFitMelt$value)), labels=round(unique(genFitOrig$value)))

points(apply(log10(genFit), 2, mean, na.rm=TRUE),pch=4)
lines(apply(log10(genFit), 2, mean, na.rm=TRUE))
dev.off

tikz("ScatterMinMeanStdDev.tex",width=6.5,height=5)
plot.default(jitter(as.numeric(gsub("V","" ,genFitMelt$variable)),factor=1),jitter(genFitMelt$value),  pch='.',  na.rm=TRUE, xlab="Generation", ylab="Fitness (Log)",yaxt="n")
axis(2, at=round(unique(genFitMelt$value)), labels=round(unique(genFitOrig$value)))

points(apply(log10(genFit), 2, mean, na.rm=TRUE),pch=4)
lines(apply(log10(genFit), 2, mean, na.rm=TRUE))
dev.off

########################################## Draw min Graph
pdf('Min.pdf',width=12,height=9)
plot(apply(genFit, 2, min, na.rm=TRUE), ylab="Fitness", xlab="Generation")
dev.off

tikz("Min.tex",width=6.5,height=5)
plot(apply(genFit, 2, min, na.rm=TRUE), ylab="Fitness", xlab="Generation")
dev.off

########
png('ScatterMinMeanStdDev.png')
plot(apply(genFit, 2, min, na.rm=TRUE), xlab="Generation", ylab="Fitness")
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
