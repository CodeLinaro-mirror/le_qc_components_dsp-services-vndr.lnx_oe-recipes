DESCRIPTION       = "Fastrpc devicetree"
LICENSE           = "GPL-2.0"
LIC_FILES_CHKSUM = "file://Makefile;md5=febcda0bf58d4266ffa5a125675c3fc3"

inherit linux-kernel-base deploy

FILESPATH   =+ "${WORKSPACE}:"
SRC_URI     =  "file://vendor/qcom/opensource/dsp-devicetree"

S = "${WORKDIR}/vendor/qcom/opensource/dsp-devicetree"

do_configure[depends] = "virtual/kernel:do_shared_workdir"

KERNEL_VERSION = "${@get_kernelversion_headers('${STAGING_KERNEL_BUILDDIR}')}"

EXTRA_OEMAKE += "TARGET_SUPPORT=${BASEMACHINE}"

# Disable parallel make
PARALLEL_MAKE = ""

# Disable parallel make
PARALLEL_MAKE = "-j1"

do_compile[lockfiles] = "${TMPDIR}/build_modules.lock"

do_configure () {
	:
}

do_compile() {
    cd ${WORKSPACE}/kernel-${PREFERRED_VERSION_linux-msm}/kernel_platform  && \
    BUILD_CONFIG=${KERNEL_BUILD_CONFIG} \
    EXT_MODULES=../../vendor/qcom/opensource/dsp-devicetree \
    ROOTDIR=${WORKSPACE}/ \
    MODULE_OUT=${S} \
    KERNEL_KIT=${KERNEL_OUT_PATH}/ \
    OUT_DIR=temp_out_dir \
    ./build/build_module.sh dtbs
}

do_deploy() {
    install -d ${DEPLOYDIR}/build-artifacts/techpack-dtbos
    cp -a \
    ${S}/${VM_KERNEL_TARGET}/*.dtbo \
    ${DEPLOYDIR}/build-artifacts/techpack-dtbos/
}

addtask do_deploy after do_install

FILES:${PN} += "${sysconfdir}/*"
