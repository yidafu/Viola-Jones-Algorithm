// Auto-generated file. Do not edit manually!
package dev.yidafu.face.detection.generated

import dev.yidafu.face.detection.CascadeClassifier
import dev.yidafu.face.detection.Feature2h
import dev.yidafu.face.detection.Feature2v
import dev.yidafu.face.detection.Feature3h
import dev.yidafu.face.detection.Feature3v
import dev.yidafu.face.detection.Feature4
import dev.yidafu.face.detection.StageClassifier
import dev.yidafu.face.detection.WeakClassifier
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.collections.List
import org.jetbrains.kotlinx.multik.ndarray.`data`.D2Array

/**
 * 训练好的级联分类器
 * 自动生成，包含 3 个阶段
 *
 * 注意：此分类器直接接收归一化后的积分图
 * 归一化和积分图计算应该在调用前完成
 */
public object TrainedCascade {
  public val stageCount: Int = 3

  public val SAMPLE_MEAN: Float = 124.45286f

  public val SAMPLE_STD: Float = 1593.4083f

  private val stage1: List<WeakClassifier> = listOf(
        WeakClassifier(
            threshold = 0.72862816f,
            polarity = -1,
            alpha = 2.8995612f,
            classifier = Feature4(x=2, y=2, width=12, height=10)
        ),
        WeakClassifier(
            threshold = 0.050201416f,
            polarity = -1,
            alpha = 2.4875097f,
            classifier = Feature2h(x=12, y=12, width=4, height=2)
        ),
        WeakClassifier(
            threshold = 0.06777859f,
            polarity = -1,
            alpha = 2.2634852f,
            classifier = Feature2v(x=8, y=13, width=2, height=4)
        )
      )

  private val stage2: List<WeakClassifier> = listOf(
        WeakClassifier(
            threshold = -0.18651247f,
            polarity = 1,
            alpha = 2.0649898f,
            classifier = Feature3v(x=9, y=10, width=2, height=6)
        ),
        WeakClassifier(
            threshold = -0.04895115f,
            polarity = 1,
            alpha = 1.9323039f,
            classifier = Feature2h(x=11, y=12, width=2, height=5)
        ),
        WeakClassifier(
            threshold = 0.005022049f,
            polarity = -1,
            alpha = 1.8866682f,
            classifier = Feature2h(x=13, y=22, width=4, height=1)
        ),
        WeakClassifier(
            threshold = 0.20396525f,
            polarity = -1,
            alpha = 2.0639849f,
            classifier = Feature2v(x=11, y=4, width=3, height=8)
        ),
        WeakClassifier(
            threshold = 0.015061617f,
            polarity = -1,
            alpha = 1.99924f,
            classifier = Feature2v(x=14, y=13, width=2, height=2)
        ),
        WeakClassifier(
            threshold = 0.12363434f,
            polarity = -1,
            alpha = 1.820572f,
            classifier = Feature2h(x=3, y=4, width=4, height=7)
        ),
        WeakClassifier(
            threshold = -0.017572463f,
            polarity = 1,
            alpha = 1.8675475f,
            classifier = Feature4(x=10, y=12, width=2, height=4)
        ),
        WeakClassifier(
            threshold = -0.026986599f,
            polarity = 1,
            alpha = 1.8644117f,
            classifier = Feature2v(x=8, y=16, width=9, height=2)
        ),
        WeakClassifier(
            threshold = 0.010668993f,
            polarity = -1,
            alpha = 1.8666092f,
            classifier = Feature2h(x=14, y=23, width=2, height=1)
        ),
        WeakClassifier(
            threshold = 0.055855095f,
            polarity = -1,
            alpha = 1.6075125f,
            classifier = Feature2v(x=15, y=13, width=2, height=4)
        ),
        WeakClassifier(
            threshold = -0.3213234f,
            polarity = 1,
            alpha = 1.8700091f,
            classifier = Feature4(x=17, y=2, width=4, height=16)
        ),
        WeakClassifier(
            threshold = 0.13618588f,
            polarity = -1,
            alpha = 1.6559967f,
            classifier = Feature4(x=9, y=10, width=14, height=4)
        ),
        WeakClassifier(
            threshold = -0.0025115013f,
            polarity = 1,
            alpha = 1.8781211f,
            classifier = Feature4(x=14, y=16, width=2, height=4)
        ),
        WeakClassifier(
            threshold = 0.3514495f,
            polarity = -1,
            alpha = 1.7945093f,
            classifier = Feature4(x=5, y=3, width=8, height=10)
        ),
        WeakClassifier(
            threshold = 0.0056476593f,
            polarity = -1,
            alpha = 1.6407933f,
            classifier = Feature4(x=11, y=11, width=2, height=4)
        )
      )

  private val stage3: List<WeakClassifier> = listOf(
        WeakClassifier(
            threshold = -0.2478981f,
            polarity = 1,
            alpha = 1.629276f,
            classifier = Feature2h(x=18, y=9, width=6, height=11)
        ),
        WeakClassifier(
            threshold = -0.054599762f,
            polarity = 1,
            alpha = 1.7919731f,
            classifier = Feature4(x=9, y=12, width=4, height=6)
        ),
        WeakClassifier(
            threshold = -0.29935932f,
            polarity = 1,
            alpha = 1.6233921f,
            classifier = Feature2h(x=5, y=9, width=4, height=9)
        ),
        WeakClassifier(
            threshold = 0.035146713f,
            polarity = -1,
            alpha = 1.6372056f,
            classifier = Feature4(x=14, y=10, width=4, height=4)
        ),
        WeakClassifier(
            threshold = -0.005021572f,
            polarity = 1,
            alpha = 1.6185136f,
            classifier = Feature2h(x=7, y=17, width=2, height=2)
        ),
        WeakClassifier(
            threshold = 0.0018820763f,
            polarity = -1,
            alpha = 1.6145927f,
            classifier = Feature2v(x=17, y=13, width=2, height=2)
        ),
        WeakClassifier(
            threshold = -0.20521986f,
            polarity = 1,
            alpha = 1.6136062f,
            classifier = Feature4(x=2, y=10, width=14, height=4)
        ),
        WeakClassifier(
            threshold = 0.03640032f,
            polarity = -1,
            alpha = 1.551465f,
            classifier = Feature2h(x=13, y=22, width=4, height=2)
        ),
        WeakClassifier(
            threshold = 0.090373516f,
            polarity = -1,
            alpha = 1.6540428f,
            classifier = Feature2v(x=10, y=12, width=8, height=6)
        ),
        WeakClassifier(
            threshold = 0.026358604f,
            polarity = -1,
            alpha = 1.6777202f,
            classifier = Feature4(x=15, y=12, width=4, height=2)
        ),
        WeakClassifier(
            threshold = 0.034517646f,
            polarity = -1,
            alpha = 1.8142543f,
            classifier = Feature4(x=11, y=3, width=2, height=18)
        ),
        WeakClassifier(
            threshold = 0.1462273f,
            polarity = -1,
            alpha = 1.7050943f,
            classifier = Feature2h(x=2, y=3, width=8, height=3)
        ),
        WeakClassifier(
            threshold = 0.0012564659f,
            polarity = -1,
            alpha = 1.7808104f,
            classifier = Feature2v(x=11, y=7, width=7, height=2)
        ),
        WeakClassifier(
            threshold = 0.01819992f,
            polarity = -1,
            alpha = 1.6272184f,
            classifier = Feature2h(x=13, y=13, width=4, height=1)
        ),
        WeakClassifier(
            threshold = 0.031380177f,
            polarity = -1,
            alpha = 1.5841377f,
            classifier = Feature2v(x=8, y=14, width=3, height=2)
        ),
        WeakClassifier(
            threshold = -0.05208963f,
            polarity = 1,
            alpha = 1.5126326f,
            classifier = Feature2h(x=6, y=23, width=8, height=1)
        ),
        WeakClassifier(
            threshold = 0.0018829703f,
            polarity = -1,
            alpha = 1.7889409f,
            classifier = Feature4(x=8, y=17, width=4, height=2)
        ),
        WeakClassifier(
            threshold = -0.17612672f,
            polarity = 1,
            alpha = 1.6136808f,
            classifier = Feature3v(x=9, y=9, width=1, height=9)
        ),
        WeakClassifier(
            threshold = -0.013179779f,
            polarity = 1,
            alpha = 1.7672015f,
            classifier = Feature2v(x=14, y=11, width=1, height=2)
        ),
        WeakClassifier(
            threshold = 0.21463442f,
            polarity = -1,
            alpha = 1.8319602f,
            classifier = Feature2v(x=12, y=3, width=4, height=12)
        ),
        WeakClassifier(
            threshold = 0.016945839f,
            polarity = -1,
            alpha = 1.699892f,
            classifier = Feature4(x=13, y=22, width=6, height=2)
        ),
        WeakClassifier(
            threshold = 0.03640032f,
            polarity = -1,
            alpha = 1.8677384f,
            classifier = Feature2h(x=12, y=12, width=4, height=2)
        ),
        WeakClassifier(
            threshold = 0.6401373f,
            polarity = -1,
            alpha = 1.9717721f,
            classifier = Feature4(x=5, y=2, width=8, height=10)
        ),
        WeakClassifier(
            threshold = -0.03702736f,
            polarity = 1,
            alpha = 1.7212751f,
            classifier = Feature2h(x=9, y=10, width=4, height=4)
        ),
        WeakClassifier(
            threshold = -0.035891056f,
            polarity = 1,
            alpha = 1.5649632f,
            classifier = Feature3h(x=1, y=23, width=12, height=1)
        ),
        WeakClassifier(
            threshold = 0.005018711f,
            polarity = 1,
            alpha = 1.6236298f,
            classifier = Feature4(x=0, y=0, width=20, height=4)
        ),
        WeakClassifier(
            threshold = 0.0056482553f,
            polarity = -1,
            alpha = 1.4858664f,
            classifier = Feature2v(x=9, y=13, width=1, height=2)
        ),
        WeakClassifier(
            threshold = -0.0025091171f,
            polarity = 1,
            alpha = 1.5777569f,
            classifier = Feature4(x=13, y=18, width=2, height=2)
        ),
        WeakClassifier(
            threshold = -0.05271715f,
            polarity = 1,
            alpha = 1.7886202f,
            classifier = Feature2v(x=11, y=7, width=4, height=12)
        ),
        WeakClassifier(
            threshold = 0.016317844f,
            polarity = -1,
            alpha = 1.7473135f,
            classifier = Feature4(x=14, y=13, width=2, height=4)
        )
      )

  private val cascade: CascadeClassifier = CascadeClassifier(
        stages = listOf(
          StageClassifier(stage1, 3.4427502f),
          StageClassifier(stage2, 15.297298f),
          StageClassifier(stage3, 32.816254f)
        )
      )

  /**
   * 检测积分图中是否包含人脸
   * @param integralImage 归一化后的积分图
   * @return true 表示包含人脸，false 表示不包含
   *
   * 注意：输入应该是已经归一化并计算好的积分图
   * 使用 ImageUtils.toIntegral() 和归一化处理
   */
  public fun detectFace(integralImage: D2Array<Float>): Boolean = cascade.predict(integralImage)
}
