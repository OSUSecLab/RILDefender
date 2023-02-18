#!/usr/bin/python3
import os
import shutil
import sys, getopt

def main(argv):
    aosp_folder = ""
    patch_folder = ""
    try:
        opts, args = getopt.getopt(argv, "ha:p:", ["aosp-folder=", "patch-folder="])
    except getopt.GetoptError:
        print('Usage: patch_apply.py -a <aosp_folder> -p <patch_folder>')
        sys.exit(2)
    if argv.__len__() < 2:
        print('Please enter at least two arguments.\nUsage: patch_apply.py -a <aosp_folder> -p <patch_folder>')
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h':
            print('Usage: patch_apply.py -a <aosp_folder> -p <patch_folder>')
            sys.exit()
        elif opt in ("-a", "--aosp-folder"):
            aosp_folder = arg
            print("AOSP root folder: " + arg)
        elif opt in ("-p", "--patch-folder"):
            patch_folder = arg
            print("Patch folder: " + arg)

    meta_file = os.path.join(patch_folder, "meta.txt")

    if not os.path.exists(meta_file):
        print("Meta file not exist!")
    else:
        with open(meta_file, 'r') as i:
            for line in i.readlines():
                action = line.strip().split("\t")[0]
                f = line.strip().split("\t")[1]
                dest = line.strip().split("\t")[2]
                src = os.path.join(patch_folder, f)
                dst = os.path.join(aosp_folder, dest)
                if action == "ADD":
                    directory = dst.replace(dst.split("/")[-1], "")
                    if not os.path.exists(directory):
                        os.mkdir(directory)
                    print("Copying %s %s" % (src, dst))
                    shutil.copy(src, dst)
                elif action == "PATCH":
                    cmd = "patch -u %s -i %s" % (dst, src)
                    print("Applying patch: " + cmd)
                    os.system(cmd)


if __name__ == "__main__":
    main(sys.argv[1:])
