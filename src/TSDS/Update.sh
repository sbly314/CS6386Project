
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
dir=$(pwd)


if [ "$command" = 'VIEW' ];then
	echo " "
	echo "---------------------Available Media Files---------------------"
	echo " "
	echo "Category: Drama"
	ls $dir/Drama -1
	ls $dir/Drama -1 > Drama.txt
	echo " "
	echo "Category: Fiction"
	ls $dir/Fiction -1 > Fiction.txt
	ls $dir/Fiction -1
	echo " "
	echo "Category: Romance"
	ls $dir/Romance -1 > Romance.txt
	ls $dir/Romance -1
	echo " "
	echo "Category: Horror"
	ls $dir/Horror -1 > Horror.txt
	ls $dir/Horror -1
echo "---------------------------------------------------------------"
fi

if [ "$command" = 'SUBSCRIBE' ];then
        ls $dir/Drama -1 > Drama.txt
        ls $dir/Fiction -1 > Fiction.txt
        ls $dir/Romance -1 > Romance.txt
        ls $dir/Horror -1 > Horror.txt
	java MediaServer > MediaServer.log &
	mv $dir/Drama/* $dir/ 
	mv $dir/Fiction/* $dir/
	mv $dir/Horror/* $dir/
        mv $dir/Romance/* $dir/
	echo "Subscribed to Media Server successfully"
echo "---------------------------------------------------------------"
fi


if [ "$command" = 'REMOVE' ];then
         echo " "
	 echo "Enter Media Name:"
	 read mediaName
	 echo " "
	 echo "Enter Category:"
         read category
         java Update $command $mediaName-$category > UpdateRemove.log &
	 rm $dir/$mediaName
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
         java Update $command $mediaName-$category > UpdateAdd.log &
         echo "$mediaName added successfully to Controller database"
echo "---------------------------------------------------------------"
fi

