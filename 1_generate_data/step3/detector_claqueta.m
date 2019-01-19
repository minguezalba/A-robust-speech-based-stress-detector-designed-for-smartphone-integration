function [indices, M, pn] = detector_claqueta(fragmento,fs)

    y = fragmento;
    
    figure
    plot(y);
    xlabel("Número de muestras");
    ylabel("Amplitud");
    title("Señal fragmento audio");

    tsamp = 0.01; % Tamaño frame/shift
    t = linspace(0,tsamp,tsamp*fs); % Eje temporal
    M = length(t); %Tamaño de trama
    N = floor(length(y)/M); % numero de tramas
    ys = reshape(y(1:(N*M)),M,N); % matriz cada columna una trama


    % Definimos rejilla de frecuencias
    fL = [697 770 852 941]; % frecuencias bajas
    fH = [1209 1336 1477 1633]; % frecuencias altas
    
    % Transformada
    omega = 2*pi*fL';
    w = exp(1j*omega*t);
    yL = abs(w*ys)/M;
    figure
    plot(yL');
    xlabel("Número de tramas");
    title("Transformada frecuencias bajas");

    omega = 2*pi*fH';
    w = exp(1j*omega*t);
    yH = abs(w*ys)/M;
    figure
    plot(yH');
    xlabel("Número de tramas");
    title("Transformada frecuencias altas");

    % Definimos umbrales para quedarnos con las freq que interesan
    ndx = 1:4;
    th1 = 0.025;
    y1 = ndx*(yL>th1);

    figure
    subplot(2,1,1);
    plot(y1);
    ylabel('low freq index');

    th2 = 0.020;
    y2 = ndx*(yH>th2);

    subplot(2,1,2);
    plot(y2);
    ylabel('high freq index');

    % look for zero to positive differences and then a few samples later

    ndx = find(diff(y1)>0); 
    % ndx contiene los indices de las tramas donde aparecen los tonos de la claqueta

    % mapping con la rejilla
    n1 = y1(ndx+2);
    n2 = y2(ndx+2);
    [n1' n2'];

    key = ['147*'; '2580'; '369#'; 'ABCD'];

    pn = char([]);
    
    indices_claqueta = find(n2);
    
    new_n1 = n1(indices_claqueta);
    new_n2 = n2(indices_claqueta);

    
    for k=1:length(new_n1)
        pn(k) =key(new_n2(k),new_n1(k));
    end

    fprintf("Tonos detectados en el fragmento: ");
    if isempty(pn)==0
        fprintf("%s \n", pn);
    else
        fprintf("Ninguno\n");
    end

    % Return
    indices = ndx; %indice en unidades de trama



end

