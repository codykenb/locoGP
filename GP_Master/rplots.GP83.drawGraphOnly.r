library('tikzDevice')
setEPS()
postscript("GPcomparisonGP83.eps")
#   tikz( 'GPcomparisonGP83.tex' )
plot(nofocusallminAvg, ylim=c(.975,1), xlab="Generation No.", ylab="Avg. Fitness", pch=c(0, rep(NA,1)))
#lines(nofocusallminAvg, type="c")
#points(nofocusallminAvg, pch=c('o', rep(NA,3)))
#lines(nofocusallminAvg, type="c")
points(R3allminAvg, pch=c(1, rep(NA,1)))
#lines(R3allminAvg, type="c")
legend(55,1,c("Canonical GP","Self-focusing GP"),pch=c(0,1),y.intersp=1.7)
dev.off()

   tikz( 'GPcomparisonGP83.tex' )
plot(nofocusallminAvg, ylim=c(.975,1), xlim=c(0,100), xlab="Generation No.", ylab="Avg. Fitness", pch=c(0, rep(NA,1)))
#lines(nofocusallminAvg, type="c")
#points(nofocusallminAvg, pch=c('o', rep(NA,3)))
#lines(nofocusallminAvg, type="c")
points(R3allminAvg, pch=c(1, rep(NA,1)))
#lines(R3allminAvg, type="c")
legend(55,1,c("Canonical GP","Self-focusing GP"),pch=c(0,1),y.intersp=1.7)
dev.off()
