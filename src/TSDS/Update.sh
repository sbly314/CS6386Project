
echo "-------------------------Media Server--------------------------"
echo " "
echo "Available Commands:"
echo " "
echo "SUBSCRIBE: This command is used to subscribe to the Controller"
echo "VIEW:      This command displays the media list"
echo "ADD:       This command is used to add a new Media File"
echo "REMOVE:    This command is used to delete a Media File"
echo "--------------------------------------------------------"
echo " "
echo "Enter Command:"
read command

if [ "$command" = 'VIEW' ];then
	echo " "
	echo "---------------------Available Media Files---------------------"
	echo " "
	echo "Category: Drama"
	ls Downloads/Drama -1
	ls Downloads/Drama -1 > Drama.txt
	echo " "
	echo "Category: Fiction"
	ls Downloads/Fiction -1 > Fiction.txt
	ls Downloads/Fiction -1
	echo " "
	echo "Category: Romance"
	ls Downloads/Romance -1 > Romance.txt
	ls Downloads/Romance -1
	echo " "
	echo "Category: Horror"
	ls Downloads/Fiction -1 > Horror.txt
	ls Downloads/Horror -1
echo "---------------------------------------------------------------"
fi

if [ "$command" = 'SUBSCRIBE' ];then
	 echo " "
       # ls Add/Drama -1 > Drama.txt
       # ls Add/Fiction -1 > Fiction.txt
       # ls Add/Romance -1 > Romance.txt
       # ls Add/Fiction -1 > Horror.txt
file=Drama.txt
#ls Downloads/Drama | wc -l
cat $file | sed -e "s/#.*//" | sed -e "/^\s*$/d" | (
    while read line
    do
     	java Update $command $line-Drama 
	echo "$line added successfully"  
    done
)
echo "---------------------------------------------------------------"
fi


if [ "$command" = 'REMOVE' ];then
         echo " "
	 echo "Enter Media Name:"
	 read mediaName
	 echo " "
	 echo "Enter Category:"
         read category
         java Update $command $mediaName-$category
	 rm Downloads/$category/$mediaName
         echo "$mediaName removed  successfully"
echo "---------------------------------------------------------------"
fi

if [ "$command" = 'ADD' ];then
         echo " "
         echo "Enter Media Name:"
         read mediaName
         echo " "
         echo "Enter Category:"
         read category
         java Update $command $mediaName-$category
         echo "$mediaName added successfully to Controller database"
echo "---------------------------------------------------------------"
fi

