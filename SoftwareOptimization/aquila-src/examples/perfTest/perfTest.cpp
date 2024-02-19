#include "aquila/global.h"
#include "aquila/source/generator/TriangleGenerator.h"
#include "aquila/source/generator/PinkNoiseGenerator.h"
#include "aquila/source/WaveFile.h"
#include "aquila/transform/FftFactory.h"
#include "aquila/transform/AquilaFft.h"
#include "aquila/transform/Dct.h"
#include "aquila/transform/Dft.h"
#include "aquila/filter/MelFilter.h"
#include "aquila/synth/KarplusStrongSynthesizer.h"
#include "aquila/ml/Dtw.h"
#include "aquila/tools/TextPlot.h"
#include <algorithm>
#include <functional>
#include <memory>
#include <time.h>


int main()
{
	Aquila::WaveFile wav("../West End Girls.wav");
    std::cout << "Loaded file: " << wav.getFilename()
              << " (" << wav.getBitsPerSample() << "b)" << std::endl;
	
    // input signal parameters
    const std::size_t SIZE = wav.getSamplesCount();
    const Aquila::FrequencyType sampleFreq = 44100;
    const Aquila::FrequencyType f_lp = 2000;
	clock_t start, end;
	start = clock();

    // calculate the FFT
    Aquila::AquilaFft* fft = new Aquila::AquilaFft(SIZE);
	auto samples=wav.toArray();
    Aquila::SpectrumType spectrum = fft->fft(samples);
	
    // generate a low-pass filter spectrum
    Aquila::SpectrumType filterSpectrum(SIZE);
    for (std::size_t i = 0; i < SIZE; ++i)
    {
        if (i < (SIZE * f_lp / sampleFreq))
        {
            // passband
            filterSpectrum[i] = 1.0;
        }
        else
        {
            // stopband
            filterSpectrum[i] = 0.0;
        }
    }

    std::transform(
        std::begin(spectrum),
        std::end(spectrum),
        std::begin(filterSpectrum),
        std::begin(spectrum),
        [] (Aquila::ComplexType x, Aquila::ComplexType y) { return x * y; }
    );
    
    // Inverse FFT moves us back to time domain
    double* x1=new double[SIZE];
    fft->ifft(spectrum, x1);

	Aquila::SignalSource ss1(x1,SIZE,sampleFreq);
	Aquila::WaveFile::save(ss1,"../West End GirlsFilteredAquila.wav");
	std::cout<<"Generated"<<std::endl;


	
	auto fft2 = Aquila::FftFactory::getFft(SIZE);
	auto samples2=wav.toArray();
    Aquila::SpectrumType spectrum2 = fft2->fft(samples2);
	
    // generate a low-pass filter spectrum
    Aquila::SpectrumType filterSpectrum2(SIZE);
    for (std::size_t i = 0; i < SIZE; ++i)
    {
        if (i < (SIZE * f_lp / sampleFreq))
        {
            // passband
            filterSpectrum2[i] = 1.0;
        }
        else
        {
            // stopband
            filterSpectrum2[i] = 0.0;
        }
    }

    std::transform(
        std::begin(spectrum2),
        std::end(spectrum2),
        std::begin(filterSpectrum2),
        std::begin(spectrum2),
        [] (Aquila::ComplexType x, Aquila::ComplexType y) { return x * y; }
    );
    
    // Inverse FFT moves us back to time domain
    double* x2=new double[SIZE];
    fft2->ifft(spectrum, x2);

	Aquila::SignalSource ss2(x2,SIZE,sampleFreq);
	Aquila::WaveFile::save(ss2,"../West End GirlsFilteredOoura.wav");
	std::cout<<"Generated"<<std::endl;	
	
	Aquila::Dft* fft3 = new Aquila::Dft(SIZE/2000);
	auto samples3=wav.toArray();
    Aquila::SpectrumType spectrum3 = fft3->fft(samples3);
	
    // generate a low-pass filter spectrum
    Aquila::SpectrumType filterSpectrum3(SIZE/2000);
    for (std::size_t i = 0; i < SIZE/2000; ++i)
    {
        if (i < ((SIZE/2000) * f_lp / sampleFreq))
        {
            // passband
            filterSpectrum3[i] = 1.0;
        }
        else
        {
            // stopband
            filterSpectrum3[i] = 0.0;
        }
    }

    std::transform(
        std::begin(spectrum3),
        std::end(spectrum3),
        std::begin(filterSpectrum3),
        std::begin(spectrum3),
        [] (Aquila::ComplexType x, Aquila::ComplexType y) { return x * y; }
    );
    
    // Inverse FFT moves us back to time domain
    double* x3=new double[SIZE/2000];
    fft3->ifft(spectrum3, x3);

	Aquila::SignalSource ss3(x3,SIZE/2000,sampleFreq);
	Aquila::WaveFile::save(ss3,"../West End GirlsFilteredDft.wav");
	std::cout<<"Generated"<<std::endl;
	
	Aquila::MelFilter mf(SIZE);
	mf.createFilter(1,200,SIZE);
	std::cout<<mf.apply(spectrum)<<std::endl;
	
	Aquila::Dtw dtw;
	std::vector<double> v1(x1, x1 + SIZE);
	std::vector<double> v2(x2, x2 + SIZE);
	std::vector<std::vector<double>> f1;
	std::vector<std::vector<double>> f2;
	f1.push_back(v1);
	f2.push_back(v2);
	
	std::cout<<dtw.getDistance(f1,f2)<<std::endl;
	
	Aquila::Dct dct;
	auto res = 	dct.dct(v1,16);
	double sum=0;
	
	for(int i=0;i<16;i++)
	{
		sum+=res[i];
	}
	std::cout<<sum<<std::endl;

	Aquila::PinkNoiseGenerator png(sampleFreq);
	png.setAmplitude(1024*64).generate(SIZE);
	Aquila::WaveFile::save(png,"../PinkNoise.wav");
	std::cout<<"Generated"<<std::endl;
	
	Aquila::TriangleGenerator tg(sampleFreq);
	tg.setFrequency(440).setAmplitude(1024*64).generate(SIZE);
	Aquila::WaveFile::save(tg,"../Triangle.wav");
	std::cout<<"Generated"<<std::endl;
	
	Aquila::KarplusStrongSynthesizer kss(sampleFreq);
	kss.playNote("C4");
	
	end = clock();
	double time_taken = double(end - start) / double(CLOCKS_PER_SEC);
	std::cout << "Time taken by program is : " << time_taken << " sec " << std::endl;
	
	delete[] x1;
	delete fft;
	delete[] x2;
	delete fft3;
	delete[] x3;

    return 0;
}
