// Auto-generated file. Do not edit manually!
package dev.yidafu.face.detection.generated

import dev.yidafu.face.detection.*
import org.jetbrains.kotlinx.multik.ndarray.data.D2Array

/**
 * 训练好的级联分类器
 * 自动生成，包含 3 个阶段
 */
object TrainedCascade {

    private val stage1: List<WeakClassifier> = listOf(
        WeakClassifier(
            threshold = 0.8099141f,
            polarity = -1,
            alpha = 3.0169332f,
            classifier = Feature4(x=2, y=3, width=12, height=10)
        ),
        WeakClassifier(
            threshold = -0.05425453f,
            polarity = 1,
            alpha = 2.97752f,
            classifier = Feature2h(x=9, y=12, width=4, height=2)
        )
    )

    private val stage2: List<WeakClassifier> = listOf(
        WeakClassifier(
            threshold = 0.22476244f,
            polarity = -1,
            alpha = 2.8160744f,
            classifier = Feature2h(x=16, y=8, width=4, height=10)
        ),
        WeakClassifier(
            threshold = 0.016792536f,
            polarity = -1,
            alpha = 2.888837f,
            classifier = Feature2v(x=15, y=13, width=1, height=2)
        ),
        WeakClassifier(
            threshold = 0.34489298f,
            polarity = -1,
            alpha = 2.9581265f,
            classifier = Feature2v(x=13, y=4, width=4, height=8)
        ),
        WeakClassifier(
            threshold = -0.032939434f,
            polarity = 1,
            alpha = 2.2701201f,
            classifier = Feature4(x=10, y=12, width=2, height=4)
        ),
        WeakClassifier(
            threshold = 0.0051668882f,
            polarity = -1,
            alpha = 2.591549f,
            classifier = Feature4(x=9, y=20, width=2, height=2)
        ),
        WeakClassifier(
            threshold = -0.7666426f,
            polarity = 1,
            alpha = 2.830857f,
            classifier = Feature4(x=10, y=0, width=10, height=12)
        ),
        WeakClassifier(
            threshold = -0.017436028f,
            polarity = 1,
            alpha = 2.935955f,
            classifier = Feature2h(x=11, y=12, width=2, height=6)
        ),
        WeakClassifier(
            threshold = 0.0032291412f,
            polarity = -1,
            alpha = 2.6799111f,
            classifier = Feature2h(x=13, y=23, width=4, height=1)
        ),
        WeakClassifier(
            threshold = 1.2355411f,
            polarity = -1,
            alpha = 2.3750737f,
            classifier = Feature2v(x=8, y=2, width=9, height=14)
        ),
        WeakClassifier(
            threshold = -0.07362878f,
            polarity = 1,
            alpha = 2.5972917f,
            classifier = Feature2h(x=6, y=6, width=2, height=11)
        )
    )

    private val stage3: List<WeakClassifier> = listOf(
        WeakClassifier(
            threshold = 0.875793f,
            polarity = -1,
            alpha = 2.723306f,
            classifier = Feature2h(x=0, y=6, width=8, height=18)
        ),
        WeakClassifier(
            threshold = 0.01743865f,
            polarity = -1,
            alpha = 2.9427335f,
            classifier = Feature4(x=14, y=11, width=4, height=2)
        ),
        WeakClassifier(
            threshold = 0.08396268f,
            polarity = -1,
            alpha = 2.792873f,
            classifier = Feature2v(x=7, y=12, width=4, height=4)
        ),
        WeakClassifier(
            threshold = 0.018084526f,
            polarity = -1,
            alpha = 2.345878f,
            classifier = Feature4(x=9, y=19, width=2, height=4)
        ),
        WeakClassifier(
            threshold = 1.0256329f,
            polarity = -1,
            alpha = 2.665663f,
            classifier = Feature4(x=3, y=2, width=12, height=10)
        ),
        WeakClassifier(
            threshold = 0.008396149f,
            polarity = -1,
            alpha = 2.4986575f,
            classifier = Feature2v(x=11, y=13, width=13, height=2)
        ),
        WeakClassifier(
            threshold = 0.37976873f,
            polarity = -1,
            alpha = 2.304679f,
            classifier = Feature2h(x=13, y=9, width=8, height=7)
        ),
        WeakClassifier(
            threshold = -0.024542809f,
            polarity = 1,
            alpha = 2.5061755f,
            classifier = Feature2h(x=11, y=13, width=2, height=2)
        ),
        WeakClassifier(
            threshold = -0.07879639f,
            polarity = 1,
            alpha = 2.5234199f,
            classifier = Feature2h(x=17, y=5, width=6, height=3)
        ),
        WeakClassifier(
            threshold = 0.91906834f,
            polarity = -1,
            alpha = 2.626331f,
            classifier = Feature2v(x=8, y=1, width=7, height=16)
        ),
        WeakClassifier(
            threshold = 0.040690422f,
            polarity = -1,
            alpha = 2.439828f,
            classifier = Feature2h(x=12, y=13, width=4, height=1)
        ),
        WeakClassifier(
            threshold = 0.0200212f,
            polarity = -1,
            alpha = 2.582533f,
            classifier = Feature2h(x=9, y=15, width=2, height=6)
        ),
        WeakClassifier(
            threshold = -0.012917042f,
            polarity = 1,
            alpha = 2.8148293f,
            classifier = Feature2v(x=14, y=11, width=1, height=2)
        ),
        WeakClassifier(
            threshold = 0.4062538f,
            polarity = -1,
            alpha = 2.5039186f,
            classifier = Feature2h(x=11, y=9, width=10, height=7)
        ),
        WeakClassifier(
            threshold = 0.005813122f,
            polarity = -1,
            alpha = 2.4053416f,
            classifier = Feature2v(x=10, y=13, width=2, height=2)
        ),
        WeakClassifier(
            threshold = 0.40689325f,
            polarity = -1,
            alpha = 2.7538443f,
            classifier = Feature4(x=4, y=1, width=6, height=14)
        ),
        WeakClassifier(
            threshold = -0.030355573f,
            polarity = 1,
            alpha = 2.7461965f,
            classifier = Feature4(x=10, y=12, width=2, height=4)
        ),
        WeakClassifier(
            threshold = 0.34618378f,
            polarity = -1,
            alpha = 2.6904404f,
            classifier = Feature2v(x=11, y=6, width=8, height=6)
        ),
        WeakClassifier(
            threshold = 0.0064587593f,
            polarity = -1,
            alpha = 2.566092f,
            classifier = Feature2v(x=15, y=13, width=1, height=2)
        ),
        WeakClassifier(
            threshold = -0.14402676f,
            polarity = 1,
            alpha = 2.7157605f,
            classifier = Feature2h(x=9, y=12, width=4, height=12)
        ),
        WeakClassifier(
            threshold = 0.36297607f,
            polarity = -1,
            alpha = 2.8720644f,
            classifier = Feature4(x=4, y=0, width=6, height=14)
        ),
        WeakClassifier(
            threshold = 0.034873962f,
            polarity = -1,
            alpha = 2.8649554f,
            classifier = Feature2h(x=16, y=10, width=4, height=3)
        ),
        WeakClassifier(
            threshold = -0.017437458f,
            polarity = 1,
            alpha = 2.317573f,
            classifier = Feature4(x=7, y=11, width=4, height=2)
        ),
        WeakClassifier(
            threshold = 0.29774284f,
            polarity = -1,
            alpha = 2.4095411f,
            classifier = Feature2v(x=12, y=4, width=3, height=10)
        ),
        WeakClassifier(
            threshold = 0.009687424f,
            polarity = -1,
            alpha = 2.7796195f,
            classifier = Feature4(x=9, y=20, width=2, height=4)
        )
    )

    private val cascade = CascadeClassifier(
        stages = listOf(
            StageClassifier(stage1, 5.395008f),
            StageClassifier(stage2, 24.249416f),
            StageClassifier(stage3, 58.853024f)
        )
    )

    /**
     * 检测积分图中是否包含人脸
     * @param integralImage 输入图像的积分图
     * @return true 表示包含人脸，false 表示不包含
     */
    fun detectFace(integralImage: D2Array<Float>): Boolean {
        return cascade.predict(integralImage)
    }
}
