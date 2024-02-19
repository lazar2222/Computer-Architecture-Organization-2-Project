/**
 * @file KarplusStrongSynthesizer.cpp
 *
 * Plucked string synthesis using Karplus-Strong algorithm.
 *
 * This file is part of the Aquila DSP library.
 * Aquila is free software, licensed under the MIT/X11 License. A copy of
 * the license is provided with the library in the LICENSE file.
 *
 * @package Aquila
 * @version 3.0.0-dev
 * @author Zbigniew Siciarz
 * @date 2007-2014
 * @license http://www.opensource.org/licenses/mit-license.php MIT
 * @since 3.0.0
 */

#include "KarplusStrongSynthesizer.h"
#include "../source/WaveFile.h"
#include <algorithm>
#include <cstddef>
#include <iostream>
#include <emmintrin.h>
#include <immintrin.h>

namespace Aquila
{
    /**
     * Plays a single note by "plucking" a string.
     *
     * A short noise burst is fed through a feedback loop including delay and
     * a first-order lowpass filter (in this case a simple moving average).
     * Resulting waveform is played using SFML - the sound is similar to a
     * plucked guitar string.
     *
     * @param frequency base frequency of the guitar note
     * @param duration tone duration in milliseconds
     */
    void KarplusStrongSynthesizer::playFrequency(FrequencyType frequency,
                                                 unsigned int duration)
    {
        std::size_t delay = static_cast<std::size_t>(m_sampleFrequency / frequency);
        std::size_t totalSamples = static_cast<std::size_t>(m_sampleFrequency * duration / 1000.0);
        m_generator.setAmplitude(8192).generate(delay);

        // copy initial noise burst at the beginning of output array
        short* arr = new short[totalSamples];
        std::copy(std::begin(m_generator), std::end(m_generator), arr);
        // first sample that goes into feedback loop;
        // cannot be averaged with previous
        arr[delay] = m_alpha * arr[0];
        std::size_t i = delay + 1;
        if(delay>=16)
        {
            short mf=m_alpha/2;
            __m256i alpha=_mm256_set_epi16(mf,mf,mf,mf,mf,mf,mf,mf,mf,mf,mf,mf,mf,mf,mf,mf);
            for (; i < totalSamples-15; i+=16)
            {
                __m256i data1=_mm256_loadu_si256((__m256i*)(arr+i-delay));
                __m256i data2=_mm256_loadu_si256((__m256i*)(arr+i-delay-1));
                data1=_mm256_add_epi16(data1,data2);
                data1=_mm256_mullo_epi16(data1,alpha);
                _mm256_storeu_si256((__m256i*)arr,data1);
            }
        }
        for (; i < totalSamples; ++i)
        {
            // average two consecutive delayed samples and dampen by alpha
             arr[i] = m_alpha * (0.5 * (arr[i - delay] + arr[i - delay - 1]));
        }

        Aquila::SignalSource ss1(arr,totalSamples,m_sampleFrequency);
	    Aquila::WaveFile::save(ss1,"../Karplus.wav");

        delete [] arr;
    }
}
