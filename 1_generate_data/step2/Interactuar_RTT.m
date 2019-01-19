function lh = task()
  % Initialize the process and its StartInfo properties.
  % The sort command is a console application that
  % reads and sorts text input.
  process = System.Diagnostics.Process;
  process.StartInfo.FileName = 'C:\Program Files\SEGGER\JLink_V512f\JLinkRTTLogger.exe';
  process.EnableRaisingEvents = true;
  process.StartInfo.CreateNoWindow = false;
  % Set UseShellExecute to false for redirection.
  process.StartInfo.UseShellExecute = false ;  %false
  %Redirect the standard output of the sort command.
  process.StartInfo.RedirectStandardOutput = true;
  % Set our event handler to asynchronously read the sort output.
  lh = process.addlistener('OutputDataReceived',@processOutputHandler);
  % Redirect standard input as well.  This stream
  % is used synchronously.
  process.StartInfo.RedirectStandardInput =true;
  % Start the process.
  process.Start();
  %Use a stream writer to synchronously write the sort input.
  ProcessStreamWriter = process.StandardInput;
  % ProcessStreamReader = process.StandardOutput;
  % Start the asynchronous read of the sort output stream.
  process.BeginOutputReadLine();

    % Obtener fecha para poder nombrar a los audios que se van grabando ->
    % obtener_fecha.m
    format shortg;

    c = clock;

    fecha = fix(c);

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

    s = strcat(AA,'_',MM,'_',DD,'_',HH,'_',MIN,'_',SS);

ProcessStreamWriter.WriteLine('NRF52832_XXAA');
ProcessStreamWriter.WriteLine('1');
ProcessStreamWriter.WriteLine('4000');
ProcessStreamWriter.WriteLine(' ');
ProcessStreamWriter.WriteLine('0');
ProcessStreamWriter.WriteLine(strcat('textos\audio_',s,'.txt'));

% ProcessStreamWriter.WriteLine('');
  %Prompt the user for 4 input text lines.  Write each
  %line to the redirected input stream of the sort command.
  numInputLines = 0;
%    inputTe = input('C:\Program Files (x86)\SEGGER\JLink_V622d\JlinkRTTLogger.exe');
%     inputTe = input('mkdir');
%   inputTe = input('\r', 's');
%    while(numInputLines ~= 4)
%       inputText = input('Enter a text line (or press the Enter key to stop):', 's');
%       numInputLines = numInputLines + 1;
%       if(~isempty(inputText))
%           ProcessStreamWriter.WriteLine(inputTe);
%       end
%   end
%   disp('end of input stream');
  %end the inputr stream to the sort command
  ProcessStreamWriter.Close();
  % wait for the sort process to write the sorted text lines
  
  process.WaitForExit();
   process.Close();
end

function processOutputHandler(obj,event)
 %collect the sort command output and print in command window
 if(~isempty(event.Data)) 
     disp(event.Data);
 end
end