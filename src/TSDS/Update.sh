
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
        ls Add/Drama -1 > Drama.txt
        ls Add/Fiction -1 > Fiction.txt
        ls Add/Romance -1 > Romance.txt
        ls Add/Fiction -1 > Horror.txt

cat $Drama.txt | sed -e "s/#.*//" | sed -e "/^\s*$/d" | (
    while read line
    do
        echo "$line"
        java Update 
        if [[ $node = $(( total-1)) ]] ; then
	   break
        fi
        node=$(( node + 1 ))
    done
)

fi
