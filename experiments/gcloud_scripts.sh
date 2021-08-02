# This experiment scripts need a Google Cloud cluster running Google Cloud
# Dataproc. The data sets are stored in a bucket named "experiments". And an 
# experimenter library has been developed containing Approx-SMOTE and the 
# "experimenter.scala" provided in this folder, which comes with the
# ClassificationExperimentLauncher.

# SUSY IR4
gcloud dataproc jobs submit spark \
    --cluster asmote --region europe-west4 \
    --class org.ubu.admirable.exec.ClassificationExperimentLauncher \
    --jars gs://experiments/experimenter.jar \
    -- gs://experiments/datasets/susy_ir4_nonorm.libsvm \
    gs://experiments/results/susy_ir4/

# SUSY IR16
gcloud dataproc jobs submit spark \
    --cluster asmote --region europe-west4 \
    --class org.ubu.admirable.exec.ClassificationExperimentLauncher \
    --jars gs://experiments/experimenter.jar \
    -- gs://experiments/datasets/susy_ir16_nonorm.libsvm \
    gs://experiments/results/susy_ir16/

# HIGGS IR4
gcloud dataproc jobs submit spark \
    --cluster asmote --region europe-west4 \
    --class org.ubu.admirable.exec.ClassificationExperimentLauncher \
    --jars gs://experiments/experimenter.jar \
    -- gs://experiments/datasets/higgs_ir4_nonorm.libsvm \
    gs://experiments/results/higgs_ir4/

# HIGGS IR16
gcloud dataproc jobs submit spark \
    --cluster asmote --region europe-west4 \
    --class org.ubu.admirable.exec.ClassificationExperimentLauncher \
    --jars gs://experiments/experimenter.jar \
    -- gs://experiments/datasets/higgs_ir16_nonorm.libsvm \
    gs://experiments/results/higgs_ir16/

# HEPMASS IR4
gcloud dataproc jobs submit spark \
    --cluster asmote --region europe-west4 \
    --class org.ubu.admirable.exec.ClassificationExperimentLauncher \
    --jars gs://experiments/experimenter.jar \
    -- gs://experiments/datasets/hepmass_ir4_nonorm.libsvm \
    gs://experiments/results/hepmass_ir4/

# HEPMASS IR16
gcloud dataproc jobs submit spark \
    --cluster asmote --region europe-west4 \
    --class org.ubu.admirable.exec.ClassificationExperimentLauncher \
    --jars gs://experiments/experimenter.jar \
    -- gs://experiments/datasets/hepmass_ir16_nonorm.libsvm \
    gs://experiments/results/hepmass_ir16/