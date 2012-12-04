#!/bin/bash  
#create hard link (or just copy) everything from $1 to $2/$1
#Ex: If you want to backup your repository 'repo' to 'repo-back', goto repo directory and use "./backup.sh . repo-back"

#Find target files and delete "^./" if there is.
for file in $(find $1 -type f | egrep -v '.svn|.class|.jar|/bin/|/gen/|~' | sed "s/\.\///")
do 
    #create directory for each file
    echo $2/$file | sed "s/\/[^\/]*\.[a-z]*$//" | xargs mkdir -p
    #create hard link for each file
    ln -f $file $2/$file
    #cp -Tufr $1$file $2$file  
done  

