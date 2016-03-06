genFit <- read.table(file="allfitness.txt",header=FALSE, nrows=-1, fill = TRUE)

na.omit(genFit)

# plot(apply(genFit, 2, min), ylim=c(.85,1), xlim=c(0,40), xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")
plot(apply(genFit, 2, min, na.rm=TRUE), xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")

png('graph.png')
plot(apply(genFit, 2, min, na.rm=TRUE), xlab="Generation No.", ylab="Fitness (Normalised)", main="Fitness Vs Generation (lower is better)")
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

