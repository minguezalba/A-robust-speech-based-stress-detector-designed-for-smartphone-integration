
format shortg;

c = clock;

fecha = fix(c);
s2 = '_';
s1 = '.txt';

for i=1:length(fecha)
   
    if i==1
        aa = fecha(1,i);
        AA = num2str(aa);
    end
    
    if i==2
       mm = fecha(1,i); 
       MM = num2str(mm);

    end
    
    if i==3
       dd = fecha(1,i);
       DD = num2str(dd);
    end
  
    if i==4
        hh = fecha(1,i);
        HH = num2str(hh);
    end
    
    if i==5
        min = fecha(1,i);
        MIN = num2str(min);
    end
    
    if i==6
        ss = fecha(1,i);
        SS = num2str(ss);
    end
end

disp(AA);
disp(MM);
disp(DD);
disp(HH);
disp(MIN);
disp(SS);

s = strcat(AA,s2,MM,s2,DD,s2,HH,s2,MIN,s2,SS,s1);
%file = fopen(s,'w');
%fclose(file);

