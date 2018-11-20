# xrepo-gradle-plugin
Cross-repository Gradle plugin modifies dependency resolution to use artifacts built from the same branch even if they're in different repository.


xrepo.enable = true|false
- czy włączyć branchowanie (można ustawić, że jakaś zmienna w env jest dostępna)

xrepo.currentBranchSrc =  nazwa zmiennej, z której pobierana jest aktualna wartość (albo w ogóle nazwa)

xrepo.disableFor = master, 
- lista nazw branchy, które nie podmieniają wersji

xrepo.fallbacks = mapa <string, string>
"hotfix/~" -> ""
"sub/[]/~" -> "\1"
"~" -> develop